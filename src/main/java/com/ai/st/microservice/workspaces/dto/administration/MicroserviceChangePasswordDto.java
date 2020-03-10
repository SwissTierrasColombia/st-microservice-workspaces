package com.ai.st.microservice.workspaces.dto.administration;

import java.io.Serializable;

public class MicroserviceChangePasswordDto implements Serializable {

	private static final long serialVersionUID = 6445260471019404726L;

	private String password;

	public MicroserviceChangePasswordDto() {

	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
