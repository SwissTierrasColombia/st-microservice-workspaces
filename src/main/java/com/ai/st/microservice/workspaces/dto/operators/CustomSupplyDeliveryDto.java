package com.ai.st.microservice.workspaces.dto.operators;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceSupplyDeliveryDto;

import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;

public class CustomSupplyDeliveryDto extends MicroserviceSupplyDeliveryDto {

    private MicroserviceSupplyDto supply;
    private MicroserviceUserDto userDownloaded;

    public CustomSupplyDeliveryDto() {
        super();
    }

    public CustomSupplyDeliveryDto(MicroserviceSupplyDeliveryDto response) {
        super();
        this.setId(response.getId());
        this.setCreatedAt(response.getCreatedAt());
        this.setDownloaded(response.getDownloaded());
        this.setDownloadedAt(response.getDownloadedAt());
        this.setObservations(response.getObservations());
        this.setSupplyCode(response.getSupplyCode());
        this.setDownloadedBy(response.getDownloadedBy());
        this.setDownloadReportUrl(response.getDownloadReportUrl());
    }


    public MicroserviceSupplyDto getSupply() {
        return supply;
    }

    public void setSupply(MicroserviceSupplyDto supply) {
        this.supply = supply;
    }

    public MicroserviceUserDto getUserDownloaded() {
        return userDownloaded;
    }

    public void setUserDownloaded(MicroserviceUserDto userDownloaded) {
        this.userDownloaded = userDownloaded;
    }

}
