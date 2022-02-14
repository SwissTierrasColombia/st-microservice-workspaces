package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.ProviderFeignClient;
import com.ai.st.microservice.common.clients.SupplyFeignClient;
import com.ai.st.microservice.common.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceExtensionDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.common.dto.supplies.*;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.operators.CustomDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.CustomSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.supplies.CustomSupplyDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;
import com.ai.st.microservice.workspaces.utils.DateTool;
import com.ai.st.microservice.workspaces.utils.FileTool;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupplyBusiness {

    @Value("${st.temporalDirectory}")
    private String stTemporalDirectory;

    private final Logger log = LoggerFactory.getLogger(SupplyBusiness.class);

    // attachments types
    public static final Long SUPPLY_ATTACHMENT_TYPE_SUPPLY = (long) 1;
    public static final Long SUPPLY_ATTACHMENT_TYPE_FTP = (long) 2;
    public static final Long SUPPLY_ATTACHMENT_TYPE_EXTERNAL_SOURCE = (long) 3;

    // states
    public static final Long SUPPLY_STATE_ACTIVE = (long) 1;
    public static final Long SUPPLY_STATE_INACTIVE = (long) 2;
    public static final Long SUPPLY_STATE_REMOVED = (long) 3;

    @Autowired
    private IMunicipalityService municipalityService;

    @Autowired
    private IWorkspaceService workspaceService;

    @Autowired
    private SupplyFeignClient supplyClient;

    @Autowired
    private ProviderFeignClient providerClient;

    @Autowired
    private OperatorMicroserviceBusiness operatorBusiness;

    public Object getSuppliesByMunicipalityAdmin(Long municipalityId, List<String> extensions, Integer page,
                                                 List<Long> requests, boolean active, Long managerCode) throws BusinessException {

        // validate if the municipality exists
        MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
        if (municipalityEntity == null) {
            throw new BusinessException("No se ha encontrado el municipio.");
        }

        return this.getSuppliesByMunicipality(municipalityEntity, extensions, page, requests, active, managerCode, null);
    }

    public Object getSuppliesByMunicipalityManager(Long municipalityId, Long managerCode, List<String> extensions,
                                                   Integer page, List<Long> requests, boolean active, Long operatorCode) throws BusinessException {

        // validate if the municipality exists
        MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
        if (municipalityEntity == null) {
            throw new BusinessException("No se ha encontrado el municipio.");
        }

        if (managerCode != null) {

            WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
            if (workspaceActive != null) {
                WorkspaceManagerEntity workspaceManagerEntity =
                        workspaceActive.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
                if (workspaceManagerEntity == null) {
                    throw new BusinessException("No tiene acceso al municipio.");
                }
            }
        }

        return this.getSuppliesByMunicipality(municipalityEntity, extensions, page, requests, active, managerCode, operatorCode);
    }

    private Object getSuppliesByMunicipality(MunicipalityEntity municipality, List<String> extensions, Integer page,
                                             List<Long> requests, boolean active, Long managerCode, Long operatorCode) throws BusinessException {

        log.info("Get supplies by municipality started");

        List<Long> states = new ArrayList<>();

        if (active) {
            states.add(SupplyBusiness.SUPPLY_STATE_ACTIVE);
        } else {
            states.add(SupplyBusiness.SUPPLY_STATE_ACTIVE);
            states.add(SupplyBusiness.SUPPLY_STATE_INACTIVE);
            states.add(SupplyBusiness.SUPPLY_STATE_REMOVED);
        }

        log.info(String.format("Params: municipality=%s extensions=%s page=%d requests=%s active=%s managerCode=%d operatorCode=%d states=%s",
                municipality.getId(), StringUtils.join(extensions, ","), page, StringUtils.join(requests, ","),
                active, managerCode, operatorCode, StringUtils.join(states, ",")));

        List<CustomSupplyDto> suppliesDto;

        try {

            MicroserviceDataPaginatedDto dataPaginated = null;

            if (page != null) {

                log.info("Get results with pagination");

                dataPaginated = supplyClient.getSuppliesByMunicipalityCodeByFilters(municipality.getCode(), page, managerCode, requests, states);

                List<? extends MicroserviceSupplyDto> response = dataPaginated.getItems();
                suppliesDto = response.stream().map(CustomSupplyDto::new).collect(Collectors.toList());

            } else {

                log.info("Get results without pagination");

                List<MicroserviceSupplyDto> response = supplyClient.getSuppliesByMunicipalityCode(municipality.getCode(), states);
                suppliesDto = response.stream().map(CustomSupplyDto::new).collect(Collectors.toList());

                if (managerCode != null) {
                    suppliesDto = suppliesDto.stream().filter(s -> s.getManagerCode().equals(managerCode)).collect(Collectors.toList());
                }

            }

            log.info("Result founds: " + suppliesDto.size());

            List<CustomSupplyDto> all = new ArrayList<>();

            for (CustomSupplyDto supplyDto : suppliesDto) {

                if (supplyDto.getTypeSupplyCode() != null) {

                    try {
                        MicroserviceTypeSupplyDto typeSupplyDto = providerClient.findTypeSuppleById(supplyDto.getTypeSupplyCode());

                        supplyDto.setTypeSupply(typeSupplyDto);

                    } catch (Exception e) {
                        throw new BusinessException("No se ha podido consultar el tipo de insumo.");
                    }

                }

                if (operatorCode != null) {
                    // verify if the supply has been delivered to operator
                    try {

                        WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipality);
                        WorkspaceOperatorEntity workspaceOperatorEntity =
                                workspaceActive.getOperators().stream().filter(o -> o.getOperatorCode().equals(operatorCode)
                                        && o.getManagerCode().equals(managerCode)).findAny().orElse(null);

                        if (workspaceOperatorEntity != null) {

                            List<CustomDeliveryDto> deliveriesDto = operatorBusiness.getDeliveriesByOperator(
                                    operatorCode, municipality.getCode());

                            for (CustomDeliveryDto deliveryFoundDto : deliveriesDto) {

                                List<? extends MicroserviceSupplyDeliveryDto> suppliesResponse = deliveryFoundDto.getSupplies();
                                List<CustomSupplyDeliveryDto> suppliesDeliveryDto =
                                        suppliesResponse.stream().map(CustomSupplyDeliveryDto::new).collect(Collectors.toList());

                                CustomSupplyDeliveryDto supplyFound = suppliesDeliveryDto.stream()
                                        .filter(sDto -> sDto.getSupplyCode().equals(supplyDto.getId())).findAny()
                                        .orElse(null);

                                if (supplyFound != null) {
                                    supplyDto.setDelivered(true);
                                    supplyDto.setDelivery(deliveryFoundDto);
                                }
                            }


                        } else {
                            throw new BusinessException("El operador no pertenece al municipio");
                        }

                    } catch (Exception e) {
                        log.error(
                                "No se ha podido consultar si el insumo ha sido entregado al operador: " + e.getMessage());
                    }
                }

                all.add(supplyDto);
            }


            if (page != null) {
                log.info("Returning results with pagination");
                dataPaginated.setItems(all);
                return dataPaginated;
            }

        } catch (Exception e) {
            throw new BusinessException("No se ha podido consultar los insumos del municipio.");
        }

        List<CustomSupplyDto> suppliesFinal = new ArrayList<>();

        if (extensions != null && extensions.size() > 0) {

            for (CustomSupplyDto supplyDto : suppliesDto) {

                if (supplyDto.getTypeSupply() != null) {
                    List<MicroserviceExtensionDto> extensionsDto = supplyDto.getTypeSupply().getExtensions();
                    for (MicroserviceExtensionDto extensionDto : extensionsDto) {

                        String extensionFound = extensions.stream().filter(
                                        extension -> extensionDto.getName().equalsIgnoreCase(extension))
                                .findAny().orElse(null);
                        if (extensionFound != null) {
                            suppliesFinal.add(supplyDto);
                        }
                    }
                }
            }

        } else {
            suppliesFinal = suppliesDto;
        }

        log.info("Returning results without pagination");
        return suppliesFinal;
    }

    public CustomSupplyDto createSupply(String municipalityCode, String observations, Long typeSupplyCode, Long toManagerCode,
                                        List<MicroserviceCreateSupplyAttachmentDto> attachments, Long requestId, Long userCode, Long providerCode,
                                        Long managerCode, Long cadastralAuthority, String modelVersion, Long stateSupplyId, String name,
                                        Boolean isValid)
            throws BusinessException {

        log.info("Create supply in supplies microservice started");

        for (MicroserviceCreateSupplyAttachmentDto attachment : attachments) {
            System.out.println("attachment ID: " + attachment.getAttachmentTypeId());
            System.out.println("attachment DATA: " + attachment.getData());
        }

        final String attachmentStringId = StringUtils.join(attachments.stream()
                .map(MicroserviceCreateSupplyAttachmentDto::getAttachmentTypeId).collect(Collectors.toList()), ",");

        log.info(String.format("params: municipality=%s observations=%s typeSupplyCode=%d toManagerCode=%d " +
                        "attachments=%s requestId=%d userCode=%d providerCode=%d managerCode=%d cadastralAuthority=%d " +
                        "modelVersion=%s stateSupplyId=%d name=%s isValid=%s", municipalityCode, observations, typeSupplyCode, toManagerCode,
                attachmentStringId, requestId, userCode, providerCode, managerCode, cadastralAuthority, modelVersion, stateSupplyId, name, isValid));

        CustomSupplyDto supplyDto;

        try {

            MicroserviceCreateSupplyDto createSupplyDto = new MicroserviceCreateSupplyDto();
            createSupplyDto.setMunicipalityCode(municipalityCode);
            createSupplyDto.setObservations(observations);
            createSupplyDto.setModelVersion(modelVersion);
            createSupplyDto.setManagerCode(toManagerCode);
            createSupplyDto.setValid(isValid);

            if (stateSupplyId != null) {
                createSupplyDto.setSupplyStateId(stateSupplyId);
            }

            if (name != null) {
                createSupplyDto.setName(name);
            }

            if (requestId != null) {
                createSupplyDto.setRequestCode(requestId);
            }

            if (typeSupplyCode != null) {
                createSupplyDto.setTypeSupplyCode(typeSupplyCode);
            }

            if (observations == null) {
                createSupplyDto.setObservations("N/A");
            }

            createSupplyDto.setAttachments(attachments);

            List<MicroserviceCreateSupplyOwnerDto> owners = new ArrayList<>();

            if (userCode != null) {
                MicroserviceCreateSupplyOwnerDto owner = new MicroserviceCreateSupplyOwnerDto();
                owner.setOwnerCode(userCode);
                owner.setOwnerType("USER");
                owners.add(owner);
            }

            if (providerCode != null) {
                MicroserviceCreateSupplyOwnerDto owner = new MicroserviceCreateSupplyOwnerDto();
                owner.setOwnerCode(providerCode);
                owner.setOwnerType("ENTITY_PROVIDER");
                owners.add(owner);
            }

            if (managerCode != null) {
                MicroserviceCreateSupplyOwnerDto owner = new MicroserviceCreateSupplyOwnerDto();
                owner.setOwnerCode(managerCode);
                owner.setOwnerType("ENTITY_MANAGER");
                owners.add(owner);
            }

            if (cadastralAuthority != null) {
                MicroserviceCreateSupplyOwnerDto owner = new MicroserviceCreateSupplyOwnerDto();
                owner.setOwnerCode(cadastralAuthority);
                owner.setOwnerType("CADASTRAL_AUTHORITY");
                owners.add(owner);
            }

            createSupplyDto.setOwners(owners);

            MicroserviceSupplyDto response = supplyClient.createSupply(createSupplyDto);
            supplyDto = new CustomSupplyDto(response);

        } catch (Exception e) {
            log.error("No se ha podido crear el insumo: " + e.getMessage());
            log.error("No se ha podido crear el insumo: " + e.getCause());
            throw new BusinessException("No se ha podido cargar el insumo");
        }

        return supplyDto;
    }

    public CustomSupplyDto getSupplyById(Long supplyId) throws BusinessException {

        CustomSupplyDto supplyDto = null;

        try {
            MicroserviceSupplyDto response = supplyClient.findSupplyById(supplyId);
            supplyDto = new CustomSupplyDto(response);

            if (supplyDto.getTypeSupplyCode() != null) {

                MicroserviceTypeSupplyDto typeSupplyDto = providerClient
                        .findTypeSuppleById(supplyDto.getTypeSupplyCode());

                supplyDto.setTypeSupply(typeSupplyDto);
            }

        } catch (Exception e) {
            log.error("No se ha podido consultar el insumo: " + e.getMessage());
        }

        return supplyDto;
    }

    public void deleteSupply(Long supplyId) {

        try {

            supplyClient.deleteSupplyById(supplyId);

        } catch (Exception e) {
            log.error("No se ha podido eliminar el insumo: " + e.getMessage());
        }

    }

    public File generateFTPFile(CustomSupplyDto supplyDto, MunicipalityDto municipalityDto) {

        String randomCode = RandomStringUtils.random(10, false, true);

        String filename = stTemporalDirectory + File.separatorChar + "insumo_" + randomCode + ".txt";

        MicroserviceSupplyAttachmentDto attachmentFtp = supplyDto.getAttachments().stream()
                .filter(a -> a.getAttachmentType().getId().equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_FTP)).findAny()
                .orElse(null);

        String content = "***********************************************" + "\n";
        content += "Sistema de transición Barrido Predial \n";
        content += "Fecha de Cargue del Insumo: " + DateTool.formatDate(supplyDto.getCreatedAt(), "yyyy-MM-dd") + "\n";
        content += "***********************************************" + "\n";
        content += "Código de Municipio: " + municipalityDto.getCode() + "\n";
        content += "Municipio: " + municipalityDto.getName() + "\n";
        content += "Departamento: " + municipalityDto.getDepartment().getName() + "\n";
        content += "***********************************************" + "\n";

        if (supplyDto.getTypeSupply() != null) {
            String typeSupplyName = supplyDto.getTypeSupply().getName().replace(" ", "_");
            content += "Nombre Insumo: " + typeSupplyName + "\n";
            content += "Proveedor: " + supplyDto.getTypeSupply().getProvider().getName() + "\n";
            content += "***********************************************" + "\n";
        }

        assert attachmentFtp != null;
        content += "URL: " + attachmentFtp.getData() + "\n";
        content += "Observaciones: " + supplyDto.getObservations() + "\n";
        content += "***********************************************" + "\n";

        return FileTool.createSimpleFile(content, filename);
    }

    public CustomSupplyDto changeStateSupply(Long supplyId, Long stateId, Long managerCode)
            throws BusinessException {

        CustomSupplyDto supplyDto = getSupplyById(supplyId);
        if (supplyDto == null) {
            throw new BusinessException("No se ha encontrado el insumo");
        }

        if (!supplyDto.getManagerCode().equals(managerCode)) {
            throw new BusinessException("No tiene acceso al insumo");
        }

        try {

            MicroserviceUpdateSupplyDto data = new MicroserviceUpdateSupplyDto();
            data.setStateId(stateId);

            MicroserviceSupplyDto response = supplyClient.updateSupply(supplyId, data);
            supplyDto = new CustomSupplyDto(response);

        } catch (BusinessException e) {
            log.error("Error actualizando el estado del insumo: " + e.getMessage());
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("Error actualizando el estado del insumo: " + e.getMessage());
            throw new BusinessException("No se ha podido cambiar el estado del insumo.");
        }

        return supplyDto;
    }

}
