package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.clients.ManagerFeignClient;
import com.ai.st.microservice.common.clients.OperatorFeignClient;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.operators.*;
import com.ai.st.microservice.common.exceptions.BusinessException;
import com.ai.st.microservice.common.exceptions.DisconnectedMicroserviceException;

import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.operators.CustomDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.CustomSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.supplies.CustomSupplyDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OperatorMicroserviceBusiness {

    private final Logger log = LoggerFactory.getLogger(OperatorMicroserviceBusiness.class);

    @Autowired
    private OperatorFeignClient operatorClient;

    @Autowired
    private ManagerFeignClient managerClient;

    @Autowired
    private AdministrationBusiness administrationBusiness;

    @Autowired
    private IMunicipalityService municipalityService;

    @Autowired
    private SupplyBusiness supplyBusiness;

    public CustomDeliveryDto createDelivery(Long operatorId, Long managerCode, String municipalityCode,
                                            String observations, List<MicroserviceCreateDeliverySupplyDto> supplies)
            throws DisconnectedMicroserviceException {

        try {

            MicroserviceCreateDeliveryDto createDeliveryDto = new MicroserviceCreateDeliveryDto();
            createDeliveryDto.setManagerCode(managerCode);
            createDeliveryDto.setMunicipalityCode(municipalityCode);
            createDeliveryDto.setObservations(observations);
            createDeliveryDto.setSupplies(supplies);

            MicroserviceDeliveryDto response = operatorClient.createDelivery(operatorId, createDeliveryDto);
            return new CustomDeliveryDto(response);

        } catch (Exception e) {
            log.error("Error creando la entrega: " + e.getMessage());
            throw new DisconnectedMicroserviceException("No se ha podido crear la entrega.");
        }
    }

    public List<CustomDeliveryDto> getDeliveriesByOperator(Long operatorId, String municipalityCode) {
        try {
            List<MicroserviceDeliveryDto> response = operatorClient.findDeliveriesByOperator(operatorId, municipalityCode);
            return response.stream().map(CustomDeliveryDto::new).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error consultando las entregas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<CustomDeliveryDto> getDeliveriesActivesByOperator(Long operatorId) throws BusinessException {

        List<CustomDeliveryDto> deliveries = new ArrayList<>();

        try {

            List<MicroserviceDeliveryDto> deliveriesResponse = operatorClient.findDeliveriesActivesByOperator(operatorId, true);

            deliveries = deliveriesResponse.stream().map(CustomDeliveryDto::new).collect(Collectors.toList());

            for (CustomDeliveryDto deliveryDto : deliveries) {

                try {
                    MicroserviceManagerDto managerDto = managerClient.findById(deliveryDto.getManagerCode());
                    deliveryDto.setManager(managerDto);
                } catch (Exception e) {
                    log.error("Error consultando gestor: " + e.getMessage());
                }

                try {
                    MunicipalityEntity municipalityEntity = municipalityService
                            .getMunicipalityByCode(deliveryDto.getMunicipalityCode());

                    MunicipalityDto municipalityDto = new MunicipalityDto();
                    municipalityDto.setCode(municipalityEntity.getCode());
                    municipalityDto.setId(municipalityEntity.getId());
                    municipalityDto.setName(municipalityEntity.getName());
                    deliveryDto.setMunicipality(municipalityDto);
                } catch (Exception e) {
                    log.error("Error consultando municipio: " + e.getMessage());
                }


                List<? extends MicroserviceSupplyDeliveryDto> suppliesResponse = deliveryDto.getSupplies();
                List<CustomSupplyDeliveryDto> supplyDeliveriesDto =
                        suppliesResponse.stream().map(CustomSupplyDeliveryDto::new).collect(Collectors.toList());

                for (CustomSupplyDeliveryDto supplyDeliveryDto : supplyDeliveriesDto) {

                    try {

                        CustomSupplyDto supplyDto = supplyBusiness
                                .getSupplyById(supplyDeliveryDto.getSupplyCode());
                        supplyDeliveryDto.setSupply(supplyDto);

                    } catch (Exception e) {
                        log.error("Error consultando insumo: " + e.getMessage());
                    }

                    if (supplyDeliveryDto.getDownloadedBy() != null) {
                        try {
                            MicroserviceUserDto userDto = administrationBusiness.getUserById(supplyDeliveryDto.getDownloadedBy());
                            supplyDeliveryDto.setUserDownloaded(userDto);
                        } catch (Exception e) {
                            log.error("Error consultando usuario: " + e.getMessage());
                        }
                    }

                }

                deliveryDto.setSupplies(supplyDeliveriesDto);

            }

        } catch (Exception e) {
            log.error("Error consultando las entregas activas: " + e.getMessage());
        }

        return deliveries;
    }

    public CustomDeliveryDto updateSupplyDeliveredDownloaded(Long deliveryId, Long supplyId, Long userCode) {
        try {
            MicroserviceUpdateDeliveredSupplyDto supplyDelivered = new MicroserviceUpdateDeliveredSupplyDto();
            supplyDelivered.setDownloaded(true);
            supplyDelivered.setDownloadedBy(userCode);
            MicroserviceDeliveryDto response = operatorClient.updateSupplyDelivered(deliveryId, supplyId, supplyDelivered);
            return new CustomDeliveryDto(response);
        } catch (Exception e) {
            log.error("Error actualizando la fecha de descarga del insumo: " + e.getMessage());
            return null;
        }
    }

    public CustomDeliveryDto disableDelivery(Long deliveryId) {
        try {
            MicroserviceDeliveryDto response = operatorClient.disableDelivery(deliveryId);
            return new CustomDeliveryDto(response);
        } catch (Exception e) {
            log.error("Error desactivando la entrega: " + e.getMessage());
            return null;
        }
    }

    public CustomDeliveryDto getDeliveryId(Long deliveryId) {
        try {
            MicroserviceDeliveryDto response = operatorClient.findDeliveryById(deliveryId);
            return new CustomDeliveryDto(response);
        } catch (Exception e) {
            log.error("Error consultando entrega: " + e.getMessage());
            return null;
        }
    }

    public MicroserviceOperatorDto getOperatorById(Long operatorId) {

        MicroserviceOperatorDto operatorDto = null;

        try {

            operatorDto = operatorClient.findById(operatorId);

        } catch (Exception e) {
            log.error("Error consultando operador: " + e.getMessage());
        }

        return operatorDto;
    }

    public MicroserviceOperatorDto addUserToOperator(Long operatorId, Long userCode) {

        MicroserviceOperatorDto operatorDto = null;

        try {

            MicroserviceAddUserToOperatorDto requestAddUser = new MicroserviceAddUserToOperatorDto();
            requestAddUser.setOperatorId(operatorId);
            requestAddUser.setUserCode(userCode);

            operatorDto = operatorClient.addUserToOperator(requestAddUser);

        } catch (Exception e) {
            log.error("Error agregando usuario al operador: " + e.getMessage());
        }

        return operatorDto;
    }

    public List<MicroserviceOperatorUserDto> getUsersByOperator(Long operatorId) {

        List<MicroserviceOperatorUserDto> users = new ArrayList<>();

        try {

            users = operatorClient.getUsersByOperator(operatorId);

        } catch (Exception e) {
            log.error("Error consultando usuarios por operador: " + e.getMessage());
        }

        return users;
    }

    public CustomDeliveryDto updateSupplyDeliveredReportURL(Long deliveryId, Long supplyId, String reportUrl) {
        try {
            MicroserviceUpdateDeliveredSupplyDto supplyDelivered = new MicroserviceUpdateDeliveredSupplyDto();
            supplyDelivered.setReportUrl(reportUrl);
            MicroserviceDeliveryDto response = operatorClient.updateSupplyDelivered(deliveryId, supplyId, supplyDelivered);
            return new CustomDeliveryDto(response);
        } catch (Exception e) {
            log.error("Error actualizando la url del reporte de descarga del insumo: " + e.getMessage());
            return null;
        }
    }

    public CustomDeliveryDto updateReportDelivery(Long deliveryId, String reportUrl) {
        try {
            MicroserviceUpdateDeliveryDto data = new MicroserviceUpdateDeliveryDto();
            data.setReportUrl(reportUrl);
            MicroserviceDeliveryDto response = operatorClient.updateDelivery(deliveryId, data);
            return new CustomDeliveryDto(response);
        } catch (Exception e) {
            log.error("Error actualizando el reporte de descarga de la entrega: " + e.getMessage());
            return null;
        }
    }

    public List<CustomDeliveryDto> getDeliveriesClosedByOperator(Long operatorId, Long municipalityId)
            throws BusinessException {

        List<CustomDeliveryDto> deliveries = new ArrayList<>();

        try {

            String municipalityCode = null;

            if (municipalityId != null) {

                MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
                if (municipalityEntity == null) {
                    throw new BusinessException("No se ha encontrado el municipio");
                }

                municipalityCode = municipalityEntity.getCode();

            }

            List<MicroserviceDeliveryDto> response = operatorClient.findDeliveriesByOperator(operatorId, municipalityCode, false);
            deliveries = response.stream().map(CustomDeliveryDto::new).collect(Collectors.toList());

            for (CustomDeliveryDto deliveryDto : deliveries) {

                deliveryDto = addInformationDelivery(deliveryDto);

            }

        } catch (Exception e) {
            log.error("Error consultando las entregas cerradas: " + e.getMessage());
        }

        return deliveries;
    }

    public List<CustomDeliveryDto> getDeliveriesByManager(Long managerId) throws BusinessException {

        List<CustomDeliveryDto> deliveries = new ArrayList<>();

        try {

            List<MicroserviceDeliveryDto> response = operatorClient.findDeliveriesByManager(managerId);
            deliveries = response.stream().map(CustomDeliveryDto::new).collect(Collectors.toList());

            for (CustomDeliveryDto deliveryDto : deliveries) {

                deliveryDto = addInformationDelivery(deliveryDto);

            }

        } catch (Exception e) {
            log.error("Error consultando las entregas: " + e.getMessage());
        }

        return deliveries;

    }

    public CustomDeliveryDto getDeliveryIdAndManager(Long deliveryId, Long managerCode) throws BusinessException {

        CustomDeliveryDto deliveryDto;

        try {
            MicroserviceDeliveryDto response = operatorClient.findDeliveryById(deliveryId);
            deliveryDto = new CustomDeliveryDto(response);
        } catch (Exception e) {
            log.error("Error consultando entrega: " + e.getMessage());
            throw new BusinessException("No se ha podido consultar la entrega");
        }

        if (!deliveryDto.getManagerCode().equals(managerCode)) {
            throw new BusinessException("La entrega no pertenece al gestor.");
        }

        deliveryDto = addInformationDelivery(deliveryDto);

        return deliveryDto;
    }

    private CustomDeliveryDto addInformationDelivery(CustomDeliveryDto deliveryDto) {

        try {
            MicroserviceManagerDto managerDto = managerClient.findById(deliveryDto.getManagerCode());
            deliveryDto.setManager(managerDto);
        } catch (Exception e) {
            log.error("Error consultando gestor: " + e.getMessage());
        }

        try {
            MunicipalityEntity municipalityEntity = municipalityService
                    .getMunicipalityByCode(deliveryDto.getMunicipalityCode());

            DepartmentEntity departmentEntity = municipalityEntity.getDepartment();
            DepartmentDto departmentDto = new DepartmentDto(departmentEntity.getId(), departmentEntity.getName(),
                    departmentEntity.getCode());

            MunicipalityDto municipalityDto = new MunicipalityDto();
            municipalityDto.setCode(municipalityEntity.getCode());
            municipalityDto.setId(municipalityEntity.getId());
            municipalityDto.setName(municipalityEntity.getName());
            municipalityDto.setDepartment(departmentDto);

            deliveryDto.setMunicipality(municipalityDto);
        } catch (Exception e) {
            log.error("Error consultando municipio: " + e.getMessage());
        }

        List<? extends MicroserviceSupplyDeliveryDto> suppliesResponse = deliveryDto.getSupplies();
        List<CustomSupplyDeliveryDto> supplyDeliveriesDto =
                suppliesResponse.stream().map(CustomSupplyDeliveryDto::new).collect(Collectors.toList());

        for (CustomSupplyDeliveryDto supplyDeliveryDto : supplyDeliveriesDto) {

            try {

                CustomSupplyDto supplyDto = supplyBusiness.getSupplyById(supplyDeliveryDto.getSupplyCode());
                supplyDeliveryDto.setSupply(supplyDto);

            } catch (Exception e) {
                log.error("Error consultando insumo: " + e.getMessage());
            }

            if (supplyDeliveryDto.getDownloadedBy() != null) {
                try {
                    MicroserviceUserDto userDto = administrationBusiness.getUserById(supplyDeliveryDto.getDownloadedBy());
                    supplyDeliveryDto.setUserDownloaded(userDto);
                } catch (Exception e) {
                    log.error("Error consultando usuario: " + e.getMessage());
                }
            }

        }

        deliveryDto.setSupplies(supplyDeliveriesDto);

        return deliveryDto;
    }

    public MicroserviceOperatorDto getOperatorByUserCode(Long userCode) {
        MicroserviceOperatorDto operatorDto;
        try {
            operatorDto = operatorClient.findByUserCode(userCode);
        } catch (Exception e) {
            return null;
        }
        return operatorDto;
    }
}
