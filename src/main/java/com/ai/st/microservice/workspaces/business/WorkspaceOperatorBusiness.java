package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.common.dto.reports.MicroserviceDownloadedSupplyDto;
import com.ai.st.microservice.common.dto.reports.MicroserviceReportInformationDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceSupplyOwnerDto;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.services.IWorkspaceOperatorService;
import com.ai.st.microservice.workspaces.utils.DateTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class WorkspaceOperatorBusiness {

    private final Logger log = LoggerFactory.getLogger(WorkspaceOperatorBusiness.class);

    private final IWorkspaceOperatorService operatorService;
    private final IWorkspaceService workspaceService;
    private final IWorkspaceOperatorService workspaceOperatorService;
    private final OperatorMicroserviceBusiness operatorBusiness;
    private final ReportBusiness reportBusiness;
    private final MunicipalityBusiness municipalityBusiness;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final SupplyBusiness supplyBusiness;
    private final AdministrationBusiness administrationBusiness;

    public WorkspaceOperatorBusiness(IWorkspaceOperatorService operatorService, IWorkspaceService workspaceService,
                                     IWorkspaceOperatorService workspaceOperatorService, OperatorMicroserviceBusiness operatorBusiness,
                                     ReportBusiness reportBusiness, MunicipalityBusiness municipalityBusiness,
                                     ManagerMicroserviceBusiness managerBusiness, SupplyBusiness supplyBusiness,
                                     AdministrationBusiness administrationBusiness) {
        this.operatorService = operatorService;
        this.workspaceService = workspaceService;
        this.workspaceOperatorService = workspaceOperatorService;
        this.operatorBusiness = operatorBusiness;
        this.reportBusiness = reportBusiness;
        this.municipalityBusiness = municipalityBusiness;
        this.managerBusiness = managerBusiness;
        this.supplyBusiness = supplyBusiness;
        this.administrationBusiness = administrationBusiness;
    }

    public MicroserviceDeliveryDto getDeliveryFromSupply(Long operatorCode, Long supplyCode) throws BusinessException {

        try {

            List<MicroserviceDeliveryDto> deliveries = operatorBusiness.getDeliveriesActivesByOperator(operatorCode);

            for (MicroserviceDeliveryDto delivery : deliveries) {

                MicroserviceSupplyDeliveryDto supplyDto = delivery.getSupplies().stream()
                        .filter(s -> s.getSupplyCode().equals(supplyCode)).findAny().orElse(null);

                if (supplyDto != null) {
                    return delivery;
                }

            }

        } catch (Exception e) {
            log.error("Error consultando la entrega por insumo: " + e.getMessage());
            throw new BusinessException("No se ha podido obtener la entrega correspondiente al insumo.");
        }

        return null;
    }

    public MicroserviceDeliveryDto registerDownloadSupply(MicroserviceDeliveryDto deliveryDto, Long supplyCode,
                                                          Long userCode) {

        try {

            MicroserviceSupplyDeliveryDto supplyDto = deliveryDto.getSupplies().stream()
                    .filter(s -> s.getSupplyCode().equals(supplyCode)).findAny().orElse(null);

            if (supplyDto != null && !supplyDto.getDownloaded()) {
                deliveryDto = operatorBusiness.updateSupplyDeliveredDownloaded(deliveryDto.getId(), supplyCode,
                        userCode);
            }

        } catch (Exception e) {
            log.error("Error consultando la entrega por insumo: " + e.getMessage());
        }

        return deliveryDto;
    }

    public MicroserviceDeliveryDto disableDelivery(Long operatorId, Long deliveryId) throws BusinessException {

        MicroserviceDeliveryDto deliveryDto = null;

        try {
            deliveryDto = operatorBusiness.getDeliveryId(deliveryId);
        } catch (Exception e) {
            log.error("Error consultando entrega por id: " + e.getMessage());
        }

        if (deliveryDto == null) {
            throw new BusinessException("No se ha encontrado la entrega.");
        }

        if (!deliveryDto.getOperator().getId().equals(operatorId)) {
            throw new BusinessException("La entrega no pertenece al operador.");
        }

        try {
            deliveryDto = operatorBusiness.disableDelivery(deliveryId);
        } catch (Exception e) {
            throw new BusinessException("No se ha podido desactivar la entrega.");
        }

        return deliveryDto;
    }

    public String generateReportDownloadSupplyIndividual(Long operatorId, Long deliveryId, Long supplyId)
            throws BusinessException {

        MicroserviceDeliveryDto deliveryDto = null;
        MicroserviceOperatorDto operatorDto = null;

        try {
            deliveryDto = operatorBusiness.getDeliveryId(deliveryId);
        } catch (Exception e) {
            log.error("Error consultando entrega por id: " + e.getMessage());
        }

        try {
            operatorDto = operatorBusiness.getOperatorById(operatorId);
        } catch (Exception e) {
            log.error("Error consultando operador por id: " + e.getMessage());
        }

        if (operatorDto == null) {
            throw new BusinessException("No se ha encontrado el operador.");
        }

        if (deliveryDto == null) {
            throw new BusinessException("No se ha encontrado la entrega.");
        }

        if (!deliveryDto.getOperator().getId().equals(operatorId)) {
            throw new BusinessException("La entrega no pertenece al operador.");
        }

        if (!deliveryDto.getIsActive()) {
            throw new BusinessException("La entrega no se encuentra activa para generar el reporte solicitado.");
        }

        MicroserviceSupplyDeliveryDto supplyDeliveryDto = deliveryDto.getSupplies().stream()
                .filter(s -> s.getSupplyCode().equals(supplyId)).findAny().orElse(null);
        if (supplyDeliveryDto == null) {
            throw new BusinessException("El insumo no pertenece a la entrega.");
        }

        if (!supplyDeliveryDto.getDownloaded()) {
            throw new BusinessException("No se puede generar el reporte porque aún no se ha descargado el reporte.");
        }

        if (supplyDeliveryDto.getDownloadReportUrl() != null && !supplyDeliveryDto.getDownloadReportUrl().isEmpty()) {
            return supplyDeliveryDto.getDownloadReportUrl();
        }

        MunicipalityDto municipalityDto = municipalityBusiness.getMunicipalityByCode(deliveryDto.getMunicipalityCode());
        if (municipalityDto == null) {
            throw new BusinessException("No se ha encontrado el municipio.");
        }

        MicroserviceManagerDto managerDto = managerBusiness.getManagerById(deliveryDto.getManagerCode());
        if (managerDto == null) {
            throw new BusinessException("No se ha encontrado el gestor.");
        }

        String format = "yyyy-MM-dd hh:mm:ss";

        // configuration parameters
        String namespace = "/" + deliveryDto.getMunicipalityCode() + "/reportes/entregas/" + deliveryDto.getId() + "/";
        String dateCreation = DateTool.formatDate(new Date(), format);
        String dateDelivery = DateTool.formatDate(deliveryDto.getCreatedAt(), format);
        String departmentName = municipalityDto.getDepartment().getName();
        String managerName = managerDto.getName();
        String municipalityCode = municipalityDto.getCode();
        String municipalityName = municipalityDto.getName();
        String observations = deliveryDto.getObservations();
        String operatorName = operatorDto.getName();

        List<MicroserviceDownloadedSupplyDto> supplies = new ArrayList<>();
        String supplyName = "";
        String providerName = "";
        MicroserviceSupplyDto supplyDto = supplyBusiness.getSupplyById(supplyDeliveryDto.getSupplyCode());
        if (supplyDto != null) {

            if (supplyDto.getTypeSupply() != null) {

                supplyName = supplyDto.getTypeSupply().getName();
                providerName = supplyDto.getTypeSupply().getProvider().getName();

            } else {

                MicroserviceSupplyOwnerDto owner = supplyDto.getOwners().stream()
                        .filter(o -> o.getOwnerType().equalsIgnoreCase("CADASTRAL_AUTHORITY")).findAny().orElse(null);
                supplyName = supplyDto.getName();
                if (owner != null) {
                    providerName = "Autoridad Catastral";
                } else {
                    providerName = "Gestor";
                }

            }

        }

        String downloadedBy = "";
        MicroserviceUserDto userDto = administrationBusiness.getUserById(supplyDeliveryDto.getDownloadedBy());
        if (userDto != null) {
            downloadedBy = userDto.getFirstName() + " " + userDto.getLastName();
        }

        supplies.add(new MicroserviceDownloadedSupplyDto(supplyName,
                DateTool.formatDate(supplyDeliveryDto.getDownloadedAt(), format), downloadedBy, providerName.toUpperCase()));

        MicroserviceReportInformationDto report = reportBusiness.generateReportDownloadSupply(namespace, dateCreation,
                dateDelivery, deliveryId.toString(), departmentName, managerName, municipalityCode, municipalityName,
                observations, operatorName, supplies);

        // update URL report
        operatorBusiness.updateSupplyDeliveredReportURL(deliveryId, supplyId, report.getUrlReport());

        return report.getUrlReport();
    }

    public String generateReportDownloadSupplyTotal(Long operatorId, Long deliveryId) throws BusinessException {

        MicroserviceDeliveryDto deliveryDto = null;
        MicroserviceOperatorDto operatorDto = null;

        try {
            deliveryDto = operatorBusiness.getDeliveryId(deliveryId);
        } catch (Exception e) {
            log.error("Error consultando entrega por id: " + e.getMessage());
        }

        try {
            operatorDto = operatorBusiness.getOperatorById(operatorId);
        } catch (Exception e) {
            log.error("Error consultando operador por id: " + e.getMessage());
        }

        if (deliveryDto == null) {
            throw new BusinessException("No se ha encontrado la entrega.");
        }

        if (operatorDto == null) {
            throw new BusinessException("No se ha encontrado el operador.");
        }

        if (!deliveryDto.getOperator().getId().equals(operatorId)) {
            throw new BusinessException("La entrega no pertenece al operador.");
        }

        for (MicroserviceSupplyDeliveryDto supplyDeliveryDto : deliveryDto.getSupplies()) {
            if (!supplyDeliveryDto.getDownloaded()) {
                throw new BusinessException(
                        "No se puede generar el reporte, porque no se han descargado todo los insumos de la entrega.");
            }
        }

        if (deliveryDto.getDownloadReportUrl() != null && !deliveryDto.getDownloadReportUrl().isEmpty()) {
            return deliveryDto.getDownloadReportUrl();
        }

        MunicipalityDto municipalityDto = municipalityBusiness.getMunicipalityByCode(deliveryDto.getMunicipalityCode());
        if (municipalityDto == null) {
            throw new BusinessException("No se ha encontrado el municipio.");
        }

        MicroserviceManagerDto managerDto = managerBusiness.getManagerById(deliveryDto.getManagerCode());
        if (managerDto == null) {
            throw new BusinessException("No se ha encontrado el gestor.");
        }

        String format = "yyyy-MM-dd hh:mm:ss";

        // configuration params
        String namespace = "/" + deliveryDto.getMunicipalityCode() + "/reportes/entregas/" + deliveryDto.getId() + "/";
        String dateCreation = DateTool.formatDate(new Date(), format);
        String dateDelivery = DateTool.formatDate(deliveryDto.getCreatedAt(), format);
        String departmentName = municipalityDto.getDepartment().getName();
        String managerName = managerDto.getName();
        String municipalityCode = municipalityDto.getCode();
        String municipalityName = municipalityDto.getName();
        String observations = deliveryDto.getObservations();
        String operatorName = operatorDto.getName();

        List<MicroserviceDownloadedSupplyDto> supplies = new ArrayList<>();
        for (MicroserviceSupplyDeliveryDto supplyDeliveryDto : deliveryDto.getSupplies()) {
            String supplyName = "";
            String providerName = "";
            MicroserviceSupplyDto supplyDto = supplyBusiness.getSupplyById(supplyDeliveryDto.getSupplyCode());
            if (supplyDto != null) {

                if (supplyDto.getTypeSupply() != null) {

                    supplyName = supplyDto.getTypeSupply().getName();
                    providerName = supplyDto.getTypeSupply().getProvider().getName();

                } else {

                    MicroserviceSupplyOwnerDto owner = supplyDto.getOwners().stream()
                            .filter(o -> o.getOwnerType().equalsIgnoreCase("CADASTRAL_AUTHORITY")).findAny()
                            .orElse(null);
                    supplyName = supplyDto.getName();
                    if (owner != null) {
                        providerName = "Autoridad Catastral";
                    } else {
                        providerName = "Gestor";
                    }

                }

            }

            String downloadedBy = "";
            MicroserviceUserDto userDto = administrationBusiness.getUserById(supplyDeliveryDto.getDownloadedBy());
            if (userDto != null) {
                downloadedBy = userDto.getFirstName() + " " + userDto.getLastName();
            }

            supplies.add(new MicroserviceDownloadedSupplyDto(supplyName,
                    DateTool.formatDate(supplyDeliveryDto.getDownloadedAt(), format), downloadedBy, providerName.toUpperCase()));
        }

        MicroserviceReportInformationDto report = reportBusiness.generateReportDownloadSupply(namespace, dateCreation,
                dateDelivery, deliveryId.toString(), departmentName, managerName, municipalityCode, municipalityName,
                observations, operatorName, supplies);

        // update url report
        operatorBusiness.updateReportDelivery(deliveryId, report.getUrlReport());

        return report.getUrlReport();
    }

    public String generateReportDeliveryManager(Long managerId, Long deliveryId) throws BusinessException {

        MicroserviceDeliveryDto deliveryDto = null;
        MicroserviceOperatorDto operatorDto = null;

        try {
            deliveryDto = operatorBusiness.getDeliveryId(deliveryId);
        } catch (Exception e) {
            log.error("Error consultando entrega por id: " + e.getMessage());
        }

        if (deliveryDto == null) {
            throw new BusinessException("No se ha encontrado la entrega.");
        }

        try {
            operatorDto = operatorBusiness.getOperatorById(deliveryDto.getOperator().getId());
        } catch (Exception e) {
            log.error("Error consultando operador por id: " + e.getMessage());
        }

        if (operatorDto == null) {
            throw new BusinessException("No se ha encontrado el operador.");
        }


        if (!deliveryDto.getManagerCode().equals(managerId)) {
            throw new BusinessException("La entrega no pertenece al gestor.");
        }

        MunicipalityDto municipalityDto = municipalityBusiness.getMunicipalityByCode(deliveryDto.getMunicipalityCode());
        if (municipalityDto == null) {
            throw new BusinessException("No se ha encontrado el municipio.");
        }

        MicroserviceManagerDto managerDto = managerBusiness.getManagerById(deliveryDto.getManagerCode());
        if (managerDto == null) {
            throw new BusinessException("No se ha encontrado el gestor.");
        }

        String format = "yyyy-MM-dd hh:mm:ss";

        // configuration params
        String namespace = "/" + deliveryDto.getMunicipalityCode() + "/reportes/entregas/" + deliveryDto.getId() + "/";
        String dateCreation = DateTool.formatDate(new Date(), format);
        String dateDelivery = DateTool.formatDate(deliveryDto.getCreatedAt(), format);
        String departmentName = municipalityDto.getDepartment().getName();
        String managerName = managerDto.getName();
        String municipalityCode = municipalityDto.getCode();
        String municipalityName = municipalityDto.getName();
        String observations = deliveryDto.getObservations();
        String operatorName = operatorDto.getName();

        List<MicroserviceDownloadedSupplyDto> supplies = new ArrayList<>();
        for (MicroserviceSupplyDeliveryDto supplyDeliveryDto : deliveryDto.getSupplies()) {
            String supplyName = "";
            String providerName = "";
            MicroserviceSupplyDto supplyDto = supplyBusiness.getSupplyById(supplyDeliveryDto.getSupplyCode());
            if (supplyDto != null) {

                if (supplyDto.getTypeSupply() != null) {

                    supplyName = supplyDto.getTypeSupply().getName();
                    providerName = supplyDto.getTypeSupply().getProvider().getName();

                } else {

                    MicroserviceSupplyOwnerDto owner = supplyDto.getOwners().stream()
                            .filter(o -> o.getOwnerType().equalsIgnoreCase("CADASTRAL_AUTHORITY")).findAny()
                            .orElse(null);
                    supplyName = supplyDto.getName();
                    if (owner != null) {
                        providerName = "Autoridad Catastral";
                    } else {
                        providerName = "Gestor";
                    }

                }

            }

            supplies.add(new MicroserviceDownloadedSupplyDto(supplyName, null, null, providerName.toUpperCase()));
        }

        MicroserviceReportInformationDto report = reportBusiness.generateReportDeliveryManager(namespace, dateCreation,
                dateDelivery, deliveryId.toString(), departmentName, managerName, municipalityCode, municipalityName,
                observations, operatorName, supplies);

        return report.getUrlReport();
    }

    public void deleteWorkspaceOperatorById(Long workspaceOperatorId) {
        operatorService.deleteWorkspaceOperatorById(workspaceOperatorId);
    }

    public WorkspaceOperatorDto createOperator(Date startDate, Date endDate, Long numberParcelsExpected, Double workArea, String observations,
                                               String supportFileURL, Long workspaceId, Long operatorCode, Long managerCode) {

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);

        WorkspaceOperatorEntity workspaceOperatorEntity = new WorkspaceOperatorEntity();

        workspaceOperatorEntity.setCreatedAt(new Date());
        workspaceOperatorEntity.setEndDate(endDate);
        workspaceOperatorEntity.setStartDate(startDate);
        workspaceOperatorEntity.setNumberParcelsExpected(numberParcelsExpected);
        workspaceOperatorEntity.setWorkArea(workArea);
        workspaceOperatorEntity.setObservations(observations);

        workspaceOperatorEntity.setWorkspace(workspaceEntity);
        workspaceOperatorEntity.setSupportFile(supportFileURL);
        workspaceOperatorEntity.setManagerCode(managerCode);
        workspaceOperatorEntity.setOperatorCode(operatorCode);

        workspaceOperatorEntity = workspaceOperatorService.createOperator(workspaceOperatorEntity);

        return entityParseToDto(workspaceOperatorEntity);
    }

    public WorkspaceOperatorDto updateWorkspaceOperator(Long workspaceManagerId, Date startDate, Date endDate,
                                                        String observations, Long parcelsExpected, Double workArea, String supportFile) {

        WorkspaceOperatorEntity workspaceOperatorEntity = workspaceOperatorService
                .getWorkspaceOperatorById(workspaceManagerId);

        if (supportFile != null && !supportFile.isEmpty()) {
            workspaceOperatorEntity.setSupportFile(supportFile);
        }

        workspaceOperatorEntity.setObservations(observations);
        workspaceOperatorEntity.setEndDate(endDate);
        workspaceOperatorEntity.setStartDate(startDate);
        workspaceOperatorEntity.setNumberParcelsExpected(parcelsExpected);
        workspaceOperatorEntity.setWorkArea(workArea);

        workspaceOperatorEntity = workspaceOperatorService.updateOperator(workspaceOperatorEntity);

        return entityParseToDto(workspaceOperatorEntity);
    }

    public WorkspaceOperatorDto entityParseToDto(WorkspaceOperatorEntity workspaceOperatorEntity) {

        WorkspaceOperatorDto workspaceOperatorDto = new WorkspaceOperatorDto();
        workspaceOperatorDto.setCreatedAt(workspaceOperatorEntity.getCreatedAt());
        workspaceOperatorDto.setEndDate(workspaceOperatorEntity.getEndDate());
        workspaceOperatorDto.setId(workspaceOperatorEntity.getId());
        workspaceOperatorDto.setNumberParcelsExpected(workspaceOperatorEntity.getNumberParcelsExpected());
        workspaceOperatorDto.setObservations(workspaceOperatorEntity.getObservations());

        workspaceOperatorDto.setOperatorCode(workspaceOperatorEntity.getOperatorCode());
        workspaceOperatorDto.setStartDate(workspaceOperatorEntity.getStartDate());
        workspaceOperatorDto.setWorkArea(workspaceOperatorEntity.getWorkArea());
        workspaceOperatorDto.setManagerCode(workspaceOperatorEntity.getManagerCode());

        MunicipalityEntity municipalityEntity = workspaceOperatorEntity.getWorkspace().getMunicipality();
        DepartmentEntity departmentEntity = municipalityEntity.getDepartment();

        MunicipalityDto municipalityDto = new MunicipalityDto();
        municipalityDto.setId(municipalityEntity.getId());
        municipalityDto.setName(municipalityEntity.getName());
        municipalityDto.setCode(municipalityEntity.getCode());
        municipalityDto.setDepartment(new DepartmentDto(departmentEntity.getId(), departmentEntity.getName(), departmentEntity.getCode()));
        workspaceOperatorDto.setMunicipality(municipalityDto);

        try {
            MicroserviceManagerDto managerDto = managerBusiness
                    .getManagerById(workspaceOperatorEntity.getManagerCode());
            workspaceOperatorDto.setManager(managerDto);
        } catch (Exception e) {
            workspaceOperatorDto.setManager(null);
        }

        try {
            MicroserviceOperatorDto operatorDto = operatorBusiness
                    .getOperatorById(workspaceOperatorEntity.getOperatorCode());
            workspaceOperatorDto.setOperator(operatorDto);
        } catch (Exception e) {
            workspaceOperatorDto.setOperator(null);
        }

        return workspaceOperatorDto;
    }

}
