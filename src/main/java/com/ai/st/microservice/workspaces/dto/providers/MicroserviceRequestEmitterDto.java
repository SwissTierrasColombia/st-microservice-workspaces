package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceRequestEmitterDto implements Serializable {

	private static final long serialVersionUID = 5475513453227858937L;

	private String emitterType;
	private Long emitterCode;

	public MicroserviceRequestEmitterDto() {

	}

	public String getEmitterType() {
		return emitterType;
	}

	public void setEmitterType(String emitterType) {
		this.emitterType = emitterType;
	}

	public Long getEmitterCode() {
		return emitterCode;
	}

	public void setEmitterCode(Long emitterCode) {
		this.emitterCode = emitterCode;
	}

}
