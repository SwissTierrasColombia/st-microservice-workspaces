package com.ai.st.microservice.workspaces.dto.supplies;

import com.ai.st.microservice.common.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceSupplyDto;

import com.ai.st.microservice.workspaces.dto.operators.CustomDeliveryDto;

import java.io.Serializable;

public class CustomSupplyDto extends MicroserviceSupplyDto implements Serializable {

    private Boolean delivered;
    private MicroserviceTypeSupplyDto typeSupply;
    private CustomDeliveryDto delivery;

    public CustomSupplyDto() {
        super();
    }

    public CustomSupplyDto(MicroserviceSupplyDto response) {
        super();
        this.setId(response.getId());
        this.setCreatedAt(response.getCreatedAt());
        this.setState(response.getState());
        this.setMunicipalityCode(response.getMunicipalityCode());
        this.setName(response.getName());
        this.setObservations(response.getObservations());
        this.setTypeSupplyCode(response.getTypeSupplyCode());
        this.setRequestCode(response.getRequestCode());
        this.setManagerCode(response.getManagerCode());
        this.setModelVersion(response.getModelVersion());
        this.setOwners(response.getOwners());
        this.setAttachments(response.getAttachments());
        this.setValid(response.getValid());
    }

    public MicroserviceTypeSupplyDto getTypeSupply() {
        return typeSupply;
    }

    public void setTypeSupply(MicroserviceTypeSupplyDto typeSupply) {
        this.typeSupply = typeSupply;
    }

    public Boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public CustomDeliveryDto getDelivery() {
        return delivery;
    }

    public void setDelivery(CustomDeliveryDto delivery) {
        this.delivery = delivery;
    }

}
