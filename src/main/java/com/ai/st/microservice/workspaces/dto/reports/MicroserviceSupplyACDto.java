package com.ai.st.microservice.workspaces.dto.reports;

import java.io.Serializable;

public class MicroserviceSupplyACDto implements Serializable {

	private static final long serialVersionUID = 1768635734498088439L;

	private String supplyName;
	private String loadedAt;
	private String observations;
	private String providerName;

	public MicroserviceSupplyACDto() {

	}

	public MicroserviceSupplyACDto(String supplyName, String loadedAt, String observations, String providerName) {
		super();
		this.supplyName = supplyName;
		this.loadedAt = loadedAt;
		this.observations = observations;
		this.providerName = providerName;
	}

	public String getSupplyName() {
		return supplyName;
	}

	public void setSupplyName(String supplyName) {
		this.supplyName = supplyName;
	}

	public String getLoadedAt() {
		return loadedAt;
	}

	public void setLoadedAt(String loadedAt) {
		this.loadedAt = loadedAt;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

}
