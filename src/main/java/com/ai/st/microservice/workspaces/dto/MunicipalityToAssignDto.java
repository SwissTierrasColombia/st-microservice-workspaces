package com.ai.st.microservice.workspaces.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MunicipalityToAssignDto")
public class MunicipalityToAssignDto {

	@ApiModelProperty(required = true, notes = "Municipality ID")
	private Long municipalityId;

	@ApiModelProperty(required = true, notes = "Observations")
	private String observations;

	public Long getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(Long municipalityId) {
		this.municipalityId = municipalityId;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

}