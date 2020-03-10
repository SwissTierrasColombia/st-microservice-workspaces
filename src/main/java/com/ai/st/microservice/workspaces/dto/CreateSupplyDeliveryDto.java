package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateSupplyDeliveryDto", description = "Create Supply Delivery Dto")
public class CreateSupplyDeliveryDto implements Serializable {

	private static final long serialVersionUID = -6022079784062012189L;

	@ApiModelProperty(required = true, notes = "Observations")
	private String observations;

	@ApiModelProperty(required = true, notes = "Supply ID")
	private Long supplyId;

	public CreateSupplyDeliveryDto() {

	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Long getSupplyId() {
		return supplyId;
	}

	public void setSupplyId(Long supplyId) {
		this.supplyId = supplyId;
	}

}
