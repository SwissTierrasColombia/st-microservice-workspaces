package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceAddAdministratorToProviderDto implements Serializable {

	private static final long serialVersionUID = -5241995478281401583L;

	private Long userCode;
	private Long providerId;
	private Long roleId;

	public MicroserviceAddAdministratorToProviderDto() {

	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public Long getProviderId() {
		return providerId;
	}

	public void setProviderId(Long providerId) {
		this.providerId = providerId;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

}
