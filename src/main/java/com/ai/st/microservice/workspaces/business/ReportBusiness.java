package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.ReportFeignClient;
import com.ai.st.microservice.common.dto.reports.*;
import com.ai.st.microservice.common.exceptions.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportBusiness {

    private final Logger log = LoggerFactory.getLogger(ReportBusiness.class);

    private final ReportFeignClient reportClient;

    public ReportBusiness(ReportFeignClient reportClient) {
        this.reportClient = reportClient;
    }

    public MicroserviceReportInformationDto generateReportDownloadSupply(String namespace, String dateCreation,
            String dateDelivery, String deliveryId, String departmentName, String managerName, String municipalityCode,
            String municipalityName, String observations, String operatorName,
            List<MicroserviceDownloadedSupplyDto> supplies) throws BusinessException {

        MicroserviceReportInformationDto informationDto;

        try {

            MicroserviceRequestReportDownloadSupplyDto data = new MicroserviceRequestReportDownloadSupplyDto();
            data.setNamespace(namespace);
            data.setDateCreation(dateCreation);
            data.setDateDelivery(dateDelivery);
            data.setDeliveryId(deliveryId);
            data.setDepartmentName(departmentName);
            data.setManagerName(managerName);
            data.setMunicipalityCode(municipalityCode);
            data.setMunicipalityName(municipalityName);
            data.setObservations(observations);
            data.setOperatorName(operatorName);
            data.setSupplies(supplies);

            informationDto = reportClient.createReportDownloadSuppliesTotal(data);
        } catch (Exception e) {
            log.error("Error creando reporte (entrega de insumos por parte del gestor al operador): " + e.getMessage());
            throw new BusinessException("No se ha podido generar el reporte.");
        }

        return informationDto;
    }

    public MicroserviceReportInformationDto generateReportDeliveryAC(String namespace, String createdAt,
            String departmentName, String managerName, String municipalityCode, String municipalityName,
            List<MicroserviceSupplyACDto> supplies) throws BusinessException {

        MicroserviceReportInformationDto informationDto;

        try {

            MicroserviceRequestReportDeliveryACDto data = new MicroserviceRequestReportDeliveryACDto();
            data.setNamespace(namespace);
            data.setCreatedAt(createdAt);

            data.setDepartmentName(departmentName);
            data.setManagerName(managerName);
            data.setMunicipalityCode(municipalityCode);
            data.setMunicipalityName(municipalityName);

            data.setSupplies(supplies);

            informationDto = reportClient.createReportDeliverySuppliesAC(data);
        } catch (Exception e) {
            log.error("Error creando reporte (entrega de insumos por parte de la autoridad catastral): "
                    + e.getMessage());
            throw new BusinessException("No se ha podido generar el reporte.");
        }

        return informationDto;

    }

    public MicroserviceReportInformationDto generateReportDeliveryManager(String namespace, String dateCreation,
            String dateDelivery, String deliveryId, String departmentName, String managerName, String municipalityCode,
            String municipalityName, String observations, String operatorName,
            List<MicroserviceDownloadedSupplyDto> supplies) throws BusinessException {

        MicroserviceReportInformationDto informationDto;

        try {

            MicroserviceRequestReportDeliveryManagerDto data = new MicroserviceRequestReportDeliveryManagerDto();
            data.setNamespace(namespace);
            data.setDateCreation(dateCreation);
            data.setDateDelivery(dateDelivery);
            data.setDeliveryId(deliveryId);
            data.setDepartmentName(departmentName);
            data.setManagerName(managerName);
            data.setMunicipalityCode(municipalityCode);
            data.setMunicipalityName(municipalityName);
            data.setObservations(observations);
            data.setOperatorName(operatorName);
            data.setSupplies(supplies);

            informationDto = reportClient.createReportDeliveryManager(data);
        } catch (Exception e) {
            log.error("Error creando reporte (entrega de insumos por parte del gestor al operador): " + e.getMessage());
            throw new BusinessException("No se ha podido generar el reporte.");
        }

        return informationDto;
    }

}
