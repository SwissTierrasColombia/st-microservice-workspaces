package com.ai.st.microservice.workspaces.dto.managers;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateManagerProfileDto", description = "Create Manager Profile Dto")
public class MicroserviceCreateManagerProfileDto implements Serializable {

	private static final long serialVersionUID = 6415729231877595624L;

	@ApiModelProperty(required = true, notes = "Description")
	private String description;

	@ApiModelProperty(required = true, notes = "Name")
	private String name;

	public MicroserviceCreateManagerProfileDto() {

	}

	public MicroserviceCreateManagerProfileDto(String description, String name) {
		this.description = description;
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
