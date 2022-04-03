package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreatePetitionDto")
public class CreatePetitionDto implements Serializable {

    private static final long serialVersionUID = -4366303440135678188L;

    @ApiModelProperty(required = true, notes = "Provider ID")
    private Long providerId;

    @ApiModelProperty(required = true, notes = "Description")
    private String description;

    public CreatePetitionDto() {

    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CreatePetitionDto{" + "providerId=" + providerId + ", description='" + description + '\'' + '}';
    }
}
