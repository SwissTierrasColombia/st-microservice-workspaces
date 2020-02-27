package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceCreateDeliveryDto implements Serializable {

	private static final long serialVersionUID = 6501515533475244560L;

	private Long managerCode;
	private String municipalityCode;
	private String observations;
	private List<MicroserviceCreateDeliverySupplyDto> supplies;

	public MicroserviceCreateDeliveryDto() {
		this.supplies = new ArrayList<MicroserviceCreateDeliverySupplyDto>();
	}

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

	public String getMunicipalityCode() {
		return municipalityCode;
	}

	public void setMunicipalityCode(String municipalityCode) {
		this.municipalityCode = municipalityCode;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public List<MicroserviceCreateDeliverySupplyDto> getSupplies() {
		return supplies;
	}

	public void setSupplies(List<MicroserviceCreateDeliverySupplyDto> supplies) {
		this.supplies = supplies;
	}

}
