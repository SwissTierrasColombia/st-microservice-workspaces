package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceProviderAdministratorDto implements Serializable {

	private static final long serialVersionUID = -4177372978182201398L;

	private Long userCode;
	private List<MicroserviceRoleDto> roles;

	public MicroserviceProviderAdministratorDto() {
		this.roles = new ArrayList<MicroserviceRoleDto>();
	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public List<MicroserviceRoleDto> getRoles() {
		return roles;
	}

	public void setRoles(List<MicroserviceRoleDto> roles) {
		this.roles = roles;
	}

}
