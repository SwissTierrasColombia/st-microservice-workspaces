package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateRequestDto", description = "Create Request Dto")
public class CreateRequestDto implements Serializable {

	private static final long serialVersionUID = 1848871808401415553L;

	@ApiModelProperty(required = true, notes = "Deadline")
	private String deadline;

	@ApiModelProperty(required = true, notes = "Supplies requested")
	private List<TypeSupplyRequestedDto> supplies;

	public CreateRequestDto() {
		supplies = new ArrayList<TypeSupplyRequestedDto>();
	}

	public String getDeadline() {
		return deadline;
	}

	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}

	public List<TypeSupplyRequestedDto> getSupplies() {
		return supplies;
	}

	public void setSupplies(List<TypeSupplyRequestedDto> supplies) {
		this.supplies = supplies;
	}

}
