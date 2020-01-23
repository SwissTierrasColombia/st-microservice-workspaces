package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;
import java.util.List;

public class MicroserviceTaskMetadataDto implements Serializable {

	private static final long serialVersionUID = 5451686139173510244L;

	private Long id;
	private String key;
	private List<MicroserviceTaskMetadataPropertyDto> properties;

	public MicroserviceTaskMetadataDto() {

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

	public List<MicroserviceTaskMetadataPropertyDto> getProperties() {
		return properties;
	}

	public void setProperties(List<MicroserviceTaskMetadataPropertyDto> properties) {
		this.properties = properties;
	}

}
