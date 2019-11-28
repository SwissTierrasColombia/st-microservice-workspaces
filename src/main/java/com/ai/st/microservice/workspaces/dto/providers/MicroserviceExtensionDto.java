package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceExtensionDto implements Serializable {

	private static final long serialVersionUID = -5373828500919545113L;

	private Long id;
	private String name;

	public MicroserviceExtensionDto() {

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
