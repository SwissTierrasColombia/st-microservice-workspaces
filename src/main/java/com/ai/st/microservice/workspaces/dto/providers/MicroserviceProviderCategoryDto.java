package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceProviderCategoryDto implements Serializable {

	private static final long serialVersionUID = -5302157804880208335L;

	private Long id;
	private String name;

	public MicroserviceProviderCategoryDto() {

	}

	public MicroserviceProviderCategoryDto(Long id, String name) {
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
