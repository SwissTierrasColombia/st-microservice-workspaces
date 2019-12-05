package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceCreateRequestDto implements Serializable {

	private static final long serialVersionUID = 1848871808401415553L;

	private String deadline;
	private Long providerId;
	private List<MicroserviceRequestEmitterDto> emitters;
	private List<MicroserviceTypeSupplyRequestedDto> supplies;
	private String municipalityCode;

	public MicroserviceCreateRequestDto() {
		supplies = new ArrayList<MicroserviceTypeSupplyRequestedDto>();
		emitters = new ArrayList<MicroserviceRequestEmitterDto>();
	}

	public String getDeadline() {
		return deadline;
	}

	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}

	public Long getProviderId() {
		return providerId;
	}

	public void setProviderId(Long providerId) {
		this.providerId = providerId;
	}

	public List<MicroserviceTypeSupplyRequestedDto> getSupplies() {
		return supplies;
	}

	public void setSupplies(List<MicroserviceTypeSupplyRequestedDto> supplies) {
		this.supplies = supplies;
	}

	public List<MicroserviceRequestEmitterDto> getEmitters() {
		return emitters;
	}

	public void setEmitters(List<MicroserviceRequestEmitterDto> emitters) {
		this.emitters = emitters;
	}

	public String getMunicipalityCode() {
		return municipalityCode;
	}

	public void setMunicipalityCode(String municipalityCode) {
		this.municipalityCode = municipalityCode;
	}

}
