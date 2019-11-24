package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceRequestStateDto implements Serializable {

	private static final long serialVersionUID = -2792776775360315658L;

	private Long id;
	private String name;

	public MicroserviceRequestStateDto() {

	}

	public MicroserviceRequestStateDto(Long id, String name) {
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
