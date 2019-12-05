package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TaskStepDto", description = "Task Step Dto")
public class MicroserviceTaskStepDto implements Serializable {

	private static final long serialVersionUID = 927981596792722859L;

	@ApiModelProperty(required = true, notes = "Step ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Step Code")
	private String code;

	@ApiModelProperty(required = true, notes = "Step Description")
	private String description;

	@ApiModelProperty(required = true, notes = "Step Status")
	private Boolean status;

	@ApiModelProperty(required = true, notes = "Type Step")
	private MicroserviceTaskTypeStepDto typeStep;

	public MicroserviceTaskStepDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public MicroserviceTaskTypeStepDto getTypeStep() {
		return typeStep;
	}

	public void setTypeStep(MicroserviceTaskTypeStepDto typeStep) {
		this.typeStep = typeStep;
	}

}
