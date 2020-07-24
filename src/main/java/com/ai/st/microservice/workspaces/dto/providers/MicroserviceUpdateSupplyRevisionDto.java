package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceUpdateSupplyRevisionDto implements Serializable {

	private static final long serialVersionUID = 1089574319167589543L;
	private Long finishedBy;

	public MicroserviceUpdateSupplyRevisionDto() {

	}

	public Long getFinishedBy() {
		return finishedBy;
	}

	public void setFinishedBy(Long finishedBy) {
		this.finishedBy = finishedBy;
	}

}
