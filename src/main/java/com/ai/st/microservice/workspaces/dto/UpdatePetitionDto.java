package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "UpdatePetitionDto")
public class UpdatePetitionDto implements Serializable {

	private static final long serialVersionUID = -5543133839868782615L;

	@ApiModelProperty(required = true, notes = "Justification")
	private String justification;

	public UpdatePetitionDto() {

	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

}
