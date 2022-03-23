package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateProviderProfileDto", description = "Create Provider Profile Dto")
public class CreateProviderProfileDto implements Serializable {

    private static final long serialVersionUID = -7565386071265746501L;

    @ApiModelProperty(required = true, notes = "Name")
    private String name;

    @ApiModelProperty(required = true, notes = "Description")
    private String description;

    public CreateProviderProfileDto() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
