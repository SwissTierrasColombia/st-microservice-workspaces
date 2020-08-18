package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;

public class MicroserviceUpdateSupplyDto implements Serializable {

	private static final long serialVersionUID = -1832068905400656799L;
	private Long stateId;

	public MicroserviceUpdateSupplyDto() {

	}

	public Long getStateId() {
		return stateId;
	}

	public void setStateId(Long stateId) {
		this.stateId = stateId;
	}

}
