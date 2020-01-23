package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;

public class MicroserviceTaskMetadataPropertyDto implements Serializable {

	private static final long serialVersionUID = 3708739121137799257L;

	private Long id;
	private String key;
	private String value;

	public MicroserviceTaskMetadataPropertyDto() {

	}

	public MicroserviceTaskMetadataPropertyDto(Long id, String key, String value) {
		this.id = id;
		this.key = key;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
