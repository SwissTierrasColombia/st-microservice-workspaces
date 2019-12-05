package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TaskStateDto", description = "Task State")
public class MicroserviceTaskStateDto implements Serializable {

	private static final long serialVersionUID = 1648940408334070826L;

	@ApiModelProperty(required = true, notes = "Task state ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Task state name")
	private String name;

	public MicroserviceTaskStateDto() {

	}

	public MicroserviceTaskStateDto(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
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

}
