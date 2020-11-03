package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "UpdateWorkspaceDto")
public class UpdateWorkpaceDto implements Serializable {

	private static final long serialVersionUID = 4887150293529893875L;

	@ApiModelProperty(required = true, notes = "Start date")
	private String startDate;

	@ApiModelProperty(required = true, notes = "Observations")
	private String observations;

	public UpdateWorkpaceDto() {

	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

}
