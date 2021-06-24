package com.ai.st.microservice.workspaces.dto.providers;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceSupplyRequestedDto;

import java.io.Serializable;

public class CustomSupplyRequestedDto extends MicroserviceSupplyRequestedDto implements Serializable {

    private MicroserviceUserDto userDeliveryBy;
    private Boolean canUpload;

    public CustomSupplyRequestedDto() {
        super();
    }

    public CustomSupplyRequestedDto(MicroserviceSupplyRequestedDto response) {
        super();
        this.setId(response.getId());
        this.setDescription(response.getDescription());
        this.setErrors(response.getErrors());
        this.setRequest(response.getRequest());
        this.setTypeSupply(response.getTypeSupply());
        this.setCreatedAt(response.getCreatedAt());
        this.setDelivered(response.getDelivered());
        this.setState(response.getState());
        this.setDeliveredAt(response.getDeliveredAt());
        this.setJustification(response.getJustification());
        this.setModelVersion(response.getModelVersion());
        this.setDeliveredBy(response.getDeliveredBy());
        this.setUrl(response.getUrl());
        this.setObservations(response.getObservations());
        this.setFtp(response.getFtp());
        this.setValid(response.getValid());
        this.setLog(response.getLog());
        this.setExtraFile(response.getExtraFile());
    }

    public Boolean getCanUpload() {
        return canUpload;
    }

    public void setCanUpload(Boolean canUpload) {
        this.canUpload = canUpload;
    }

    public MicroserviceUserDto getUserDeliveryBy() {
        return userDeliveryBy;
    }

    public void setUserDeliveryBy(MicroserviceUserDto userDeliveryBy) {
        this.userDeliveryBy = userDeliveryBy;
    }

}
