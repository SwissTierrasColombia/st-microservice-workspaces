package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TypeSupplyRequestDto", description = "Type Supply Requested Dto")
public class TypeSupplyRequestedDto implements Serializable {

    private static final long serialVersionUID = -5598899972451538583L;

    @ApiModelProperty(required = true, notes = "Deadline")
    private Long typeSupplyId;

    @ApiModelProperty(notes = "Observation")
    private String observation;

    @ApiModelProperty(required = true, notes = "Provider ID")
    private Long providerId;

    @ApiModelProperty(notes = "Model version")
    private String modelVersion;

    public TypeSupplyRequestedDto() {

    }

    public Long getTypeSupplyId() {
        return typeSupplyId;
    }

    public void setTypeSupplyId(Long typeSupplyId) {
        this.typeSupplyId = typeSupplyId;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    @Override
    public String toString() {
        return "TypeSupplyRequestedDto{" + "typeSupplyId=" + typeSupplyId + ", observation='" + observation + '\''
                + ", providerId=" + providerId + ", modelVersion='" + modelVersion + '\'' + '}';
    }
}
