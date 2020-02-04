package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceTypeSupplyRequestedDto implements Serializable {

	private static final long serialVersionUID = -5598899972451538583L;

	private Long typeSupplyId;
	private String observation;
	private String modelVersion;

	public MicroserviceTypeSupplyRequestedDto() {

	}

	public Long getTypeSupplyId() {
		return typeSupplyId;
	}

	public void setTypeSupplyId(Long typeSupplyId) {
		this.typeSupplyId = typeSupplyId;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

}
