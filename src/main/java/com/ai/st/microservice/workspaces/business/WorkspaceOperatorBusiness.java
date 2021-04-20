package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceOperatorDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceDownloadedSupplyDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceReportInformationDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyOwnerDto;
import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IWorkspaceOperatorService;
import com.ai.st.microservice.workspaces.utils.DateTool;

@Component
public class WorkspaceOperatorBusiness {

    private final Logger log = LoggerFactory.getLogger(WorkspaceOperatorBusiness.class);

    @Autowired
    private IWorkspaceOperatorService operatorService;

    @Autowired
    private IWorkspaceService workspaceService;

    @Autowired
    private IWorkspaceOperatorService workspaceOperatorService;

    @Autowired
    private OperatorBusiness operatorBusiness;

    @Autowired
    private ReportBusiness reportBusiness;

    @Autowired
    private MunicipalityBusiness municipalityBusiness;

    @Autowired
    private ManagerBusiness managerBusiness;

    @Autowired
    private SupplyBusiness supplyBusiness;

    @Autowired
    private UserBusiness userBusiness;

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
                if (owner != null) {
                    supplyName = supplyDto.getName();
                    providerName = "Autoridad Catastral";
                }

            }

        }

        String downloadedBy = "";
        MicroserviceUserDto userDto = userBusiness.getUserById(supplyDeliveryDto.getDownloadedBy());
        if (userDto != null) {
            downloadedBy = userDto.getFirstName() + " " + userDto.getLastName();
        }

        supplies.add(new MicroserviceDownloadedSupplyDto(supplyName,
                DateTool.formatDate(supplyDeliveryDto.getDownloadedAt(), format), downloadedBy, providerName));

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
                    if (owner != null) {
                        supplyName = supplyDto.getName();
                        providerName = "Autoridad Catastral";
                    }

                }

            }

            String downloadedBy = "";
            MicroserviceUserDto userDto = userBusiness.getUserById(supplyDeliveryDto.getDownloadedBy());
            if (userDto != null) {
                downloadedBy = userDto.getFirstName() + " " + userDto.getLastName();
            }

            supplies.add(new MicroserviceDownloadedSupplyDto(supplyName,
                    DateTool.formatDate(supplyDeliveryDto.getDownloadedAt(), format), downloadedBy, providerName));
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
                    if (owner != null) {
                        supplyName = supplyDto.getName();
                        providerName = "Autoridad Catastral";
                    }

                }

            }

            supplies.add(new MicroserviceDownloadedSupplyDto(supplyName, null, null, providerName));
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
