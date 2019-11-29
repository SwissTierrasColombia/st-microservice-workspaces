package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateUserDto", description = "Create User Dto")
public class CreateUserDto implements Serializable {

	private static final long serialVersionUID = -2041172889834251468L;

	@ApiModelProperty(required = true, notes = "First name")
	private String firstName;

	@ApiModelProperty(required = true, notes = "Last name")
	private String lastName;

	@ApiModelProperty(required = true, notes = "Username")
	private String username;

	@ApiModelProperty(required = true, notes = "Email")
	private String email;

	@ApiModelProperty(required = true, notes = "Password")
	private String password;

	@ApiModelProperty(required = false, notes = "Roles")
	private CreateUserRoleProviderDto roleProvider;

	public CreateUserDto() {

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public CreateUserRoleProviderDto getRoleProvider() {
		return roleProvider;
	}

	public void setRoleProvider(CreateUserRoleProviderDto roleProvider) {
		this.roleProvider = roleProvider;
	}

}
