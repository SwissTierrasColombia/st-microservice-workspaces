package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceCreatePetitionDto implements Serializable {

	private static final long serialVersionUID = -1257173806267524291L;

	private String observations;
	private Long managerCode;

	public MicroserviceCreatePetitionDto() {

	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

}
