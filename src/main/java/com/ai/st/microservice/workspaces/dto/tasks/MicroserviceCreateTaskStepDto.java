package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;

public class MicroserviceCreateTaskStepDto implements Serializable {

	private static final long serialVersionUID = 2220658679258020082L;

	private String title;
	private String description;
	private Long typeStepId;

	public MicroserviceCreateTaskStepDto() {

	}

	public MicroserviceCreateTaskStepDto(String title, String description, Long typeStepId) {
		this.title = title;
		this.description = description;
		this.typeStepId = typeStepId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getTypeStepId() {
		return typeStepId;
	}

	public void setTypeStepId(Long typeStepId) {
		this.typeStepId = typeStepId;
	}

}
