package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;

public class MicroserviceCreateDeliverySupplyDto implements Serializable {

	private static final long serialVersionUID = 2494274349120415860L;

	private String observations;
	private Long supplyCode;

	public MicroserviceCreateDeliverySupplyDto() {

	}

	public MicroserviceCreateDeliverySupplyDto(String observations, Long supplyCode) {
		super();
		this.observations = observations;
		this.supplyCode = supplyCode;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Long getSupplyCode() {
		return supplyCode;
	}

	public void setSupplyCode(Long supplyCode) {
		this.supplyCode = supplyCode;
	}

}
