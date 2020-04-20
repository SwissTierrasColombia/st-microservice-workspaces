package com.ai.st.microservice.workspaces.dto.administration;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "UpdateUserDto", description = "Create User Dto")
public class MicroserviceUpdateUserDto implements Serializable {

	private static final long serialVersionUID = -2041172889834251468L;

	@ApiModelProperty(required = true, notes = "First name")
	private String firstName;

	@ApiModelProperty(required = true, notes = "Last name")
	private String lastName;

	public MicroserviceUpdateUserDto() {

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

}
