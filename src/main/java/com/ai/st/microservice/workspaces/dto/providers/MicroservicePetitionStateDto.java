package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroservicePetitionStateDto implements Serializable {

	private static final long serialVersionUID = -945178753218052140L;
	
	private Long id;
	private String name;

	public MicroservicePetitionStateDto() {

	}

	public MicroservicePetitionStateDto(Long id, String name) {
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
