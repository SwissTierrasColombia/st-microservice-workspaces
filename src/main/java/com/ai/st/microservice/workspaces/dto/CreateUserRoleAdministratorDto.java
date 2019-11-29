package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateUserRoleAdministratorDto", description = "Create User Role Administrator Dto")
public class CreateUserRoleAdministratorDto implements Serializable {

	private static final long serialVersionUID = -6386496439475467133L;

	@ApiModelProperty(required = true, notes = "Role ID")
	private Long roleId;

	public CreateUserRoleAdministratorDto() {

	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

}
