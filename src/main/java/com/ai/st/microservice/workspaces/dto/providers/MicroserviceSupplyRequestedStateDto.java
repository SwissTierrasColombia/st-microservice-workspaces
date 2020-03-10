package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceSupplyRequestedStateDto implements Serializable {

	private static final long serialVersionUID = 1008543219005393218L;

	private Long id;
	private String name;

	public MicroserviceSupplyRequestedStateDto() {

	}

	public MicroserviceSupplyRequestedStateDto(Long id, String name) {
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
