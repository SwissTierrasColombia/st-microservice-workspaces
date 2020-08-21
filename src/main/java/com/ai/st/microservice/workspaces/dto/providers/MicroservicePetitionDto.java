package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.Date;

import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;

public class MicroservicePetitionDto implements Serializable {

	private static final long serialVersionUID = 6001978070953258795L;

	private Long id;
	private Date createdAt;
	private Long managerCode;
	private MicroserviceManagerDto manager;
	private String observations;
	private String justification;
	private MicroservicePetitionStateDto petitionState;
	private MicroserviceProviderDto provider;

	public MicroservicePetitionDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public MicroservicePetitionStateDto getPetitionState() {
		return petitionState;
	}

	public void setPetitionState(MicroservicePetitionStateDto petitionState) {
		this.petitionState = petitionState;
	}

	public MicroserviceProviderDto getProvider() {
		return provider;
	}

	public void setProvider(MicroserviceProviderDto provider) {
		this.provider = provider;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public MicroserviceManagerDto getManager() {
		return manager;
	}

	public void setManager(MicroserviceManagerDto manager) {
		this.manager = manager;
	}

}
