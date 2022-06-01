package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "IntegrationStateDto", description = "Integration State Dto")
public class IntegrationStateDto implements Serializable {

    private static final long serialVersionUID = 4121667515330552261L;

    @ApiModelProperty(required = true, notes = "Integration State ID")
    private Long id;

    @ApiModelProperty(required = true, notes = "Name")
    private String name;

    @ApiModelProperty(required = true, notes = "Description")
    private String description;

    public IntegrationStateDto() {

    }

    public IntegrationStateDto(Long id, String name, String description) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
