package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;

public class MicroserviceCancelTaskDto implements Serializable {

	private static final long serialVersionUID = -8968447263161444063L;

	private String reason;

	public MicroserviceCancelTaskDto() {

	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
