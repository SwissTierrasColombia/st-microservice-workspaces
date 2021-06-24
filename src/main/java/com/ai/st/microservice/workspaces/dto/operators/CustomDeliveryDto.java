package com.ai.st.microservice.workspaces.dto.operators;

import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceDeliveryDto;

import com.ai.st.microservice.workspaces.dto.MunicipalityDto;

public class CustomDeliveryDto extends MicroserviceDeliveryDto {

    private MicroserviceManagerDto manager;
    private MunicipalityDto municipality;

    public CustomDeliveryDto() {
        super();
    }

    public CustomDeliveryDto(MicroserviceDeliveryDto response) {
        super();
        this.setId(response.getId());
        this.setCreatedAt(response.getCreatedAt());
        this.setManagerCode(response.getManagerCode());
        this.setMunicipalityCode(response.getMunicipalityCode());
        this.setIsActive(response.getIsActive());
        this.setObservations(response.getObservations());
        this.setOperator(response.getOperator());
        this.setDownloadReportUrl(response.getDownloadReportUrl());
        this.setSupplies(response.getSupplies());
    }

    public MicroserviceManagerDto getManager() {
        return manager;
    }

    public void setManager(MicroserviceManagerDto manager) {
        this.manager = manager;
    }

    public MunicipalityDto getMunicipality() {
        return municipality;
    }

    public void setMunicipality(MunicipalityDto municipality) {
        this.municipality = municipality;
    }

}
