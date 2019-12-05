package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceProviderProfileDto implements Serializable {

	private static final long serialVersionUID = -557539358130899883L;

	private Long id;
	private String description;
	private String name;

	public MicroserviceProviderProfileDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
