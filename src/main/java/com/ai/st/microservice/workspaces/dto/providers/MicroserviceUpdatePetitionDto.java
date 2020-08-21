package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceUpdatePetitionDto implements Serializable {

	private static final long serialVersionUID = -3550075435340827828L;

	private Long petitionStateId;
	private String justitication;

	public MicroserviceUpdatePetitionDto() {

	}

	public Long getPetitionStateId() {
		return petitionStateId;
	}

	public void setPetitionStateId(Long petitionStateId) {
		this.petitionStateId = petitionStateId;
	}

	public String getJustitication() {
		return justitication;
	}

	public void setJustitication(String justitication) {
		this.justitication = justitication;
	}

}
