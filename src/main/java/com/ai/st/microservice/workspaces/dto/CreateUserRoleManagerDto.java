package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateUserRoleManagerDto", description = "Create User Role Manager Dto")
public class CreateUserRoleManagerDto implements Serializable {

	private static final long serialVersionUID = 4040750533290642970L;

	@ApiModelProperty(required = true, notes = "Role ID")
	private Long roleId;

	@ApiModelProperty(required = true, notes = "Manager ID")
	private Long managerId;

	@ApiModelProperty(required = true, notes = "Profiles ID")
	private List<Long> profiles;
	
	private Boolean isManager;
	private Boolean isDirector;

	public CreateUserRoleManagerDto() {
		this.profiles = new ArrayList<Long>();
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

	public List<Long> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<Long> profiles) {
		this.profiles = profiles;
	}

	public Boolean getIsManager() {
		return isManager;
	}

	public void setIsManager(Boolean isManager) {
		this.isManager = isManager;
	}

	public Boolean getIsDirector() {
		return isDirector;
	}

	public void setIsDirector(Boolean isDirector) {
		this.isDirector = isDirector;
	}

}
