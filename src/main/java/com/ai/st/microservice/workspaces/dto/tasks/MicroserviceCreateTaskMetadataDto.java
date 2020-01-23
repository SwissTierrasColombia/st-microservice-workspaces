package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceCreateTaskMetadataDto implements Serializable {

	private static final long serialVersionUID = 3915775195611030554L;

	private String key;
	private List<MicroserviceCreateTaskPropertyDto> properties;

	public MicroserviceCreateTaskMetadataDto() {
		this.properties = new ArrayList<>();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<MicroserviceCreateTaskPropertyDto> getProperties() {
		return properties;
	}

	public void setProperties(List<MicroserviceCreateTaskPropertyDto> properties) {
		this.properties = properties;
	}

}
