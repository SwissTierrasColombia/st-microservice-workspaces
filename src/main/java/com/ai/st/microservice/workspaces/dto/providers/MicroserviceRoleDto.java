package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceRoleDto implements Serializable {

	private static final long serialVersionUID = -8478165185005520773L;

	private Long id;
	private String name;

	public MicroserviceRoleDto() {

	}

	public MicroserviceRoleDto(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
