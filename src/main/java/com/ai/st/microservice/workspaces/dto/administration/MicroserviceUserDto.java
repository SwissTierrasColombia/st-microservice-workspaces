package com.ai.st.microservice.workspaces.dto.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderProfileDto;

public class MicroserviceUserDto implements Serializable {

	private static final long serialVersionUID = -5121529899322990688L;

	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String username;
	private String password;
	private Boolean enabled;
	private Date createdAt;
	private Date updatedAt;
	private List<MicroserviceRoleDto> roles;
	private List<MicroserviceManagerProfileDto> profilesManager;
	private List<MicroserviceProviderProfileDto> profilesProvider;
	private List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> rolesProvider;
	private Object entity;

	public MicroserviceUserDto() {
		this.roles = new ArrayList<MicroserviceRoleDto>();
		this.profilesManager = new ArrayList<>();
		this.profilesProvider = new ArrayList<>();
		this.rolesProvider = new ArrayList<>();
	}

	public MicroserviceUserDto(Long id, String firstName, String lastName, String email, String username) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.username = username;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<MicroserviceRoleDto> getRoles() {
		return roles;
	}

	public void setRoles(List<MicroserviceRoleDto> roles) {
		this.roles = roles;
	}

	public List<MicroserviceManagerProfileDto> getProfilesManager() {
		return profilesManager;
	}

	public void setProfilesManager(List<MicroserviceManagerProfileDto> profilesManager) {
		this.profilesManager = profilesManager;
	}

	public List<MicroserviceProviderProfileDto> getProfilesProvider() {
		return profilesProvider;
	}

	public void setProfilesProvider(List<MicroserviceProviderProfileDto> profilesProvider) {
		this.profilesProvider = profilesProvider;
	}

	public List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> getRolesProvider() {
		return rolesProvider;
	}

	public void setRolesProvider(
			List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> rolesProvider) {
		this.rolesProvider = rolesProvider;
	}

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

}
