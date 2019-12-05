package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TaskTypeStepDto", description = "Task Type Step Dto")
public class MicroserviceTaskTypeStepDto implements Serializable {

	private static final long serialVersionUID = 5382565385513050513L;

	@ApiModelProperty(required = true, notes = "Task type step ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Task type step name")
	private String name;

	public MicroserviceTaskTypeStepDto() {

	}

	public MicroserviceTaskTypeStepDto(Long id, String name) {
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
