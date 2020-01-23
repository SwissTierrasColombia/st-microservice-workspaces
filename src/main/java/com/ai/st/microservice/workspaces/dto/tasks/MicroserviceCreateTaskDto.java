package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceCreateTaskDto implements Serializable {

	private static final long serialVersionUID = 8405437490199078160L;

	private String name;
	private String description;
	private String deadline;
	private List<Long> users;
	private List<Long> categories;
	private List<MicroserviceCreateTaskMetadataDto> metadata;
	private List<MicroserviceCreateTaskStepDto> steps;

	public MicroserviceCreateTaskDto() {
		this.users = new ArrayList<Long>();
		this.steps = new ArrayList<MicroserviceCreateTaskStepDto>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDeadline() {
		return deadline;
	}

	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}

	public List<Long> getUsers() {
		return users;
	}

	public void setUsers(List<Long> users) {
		this.users = users;
	}

	public List<Long> getCategories() {
		return categories;
	}

	public void setCategories(List<Long> categories) {
		this.categories = categories;
	}

	public List<MicroserviceCreateTaskMetadataDto> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<MicroserviceCreateTaskMetadataDto> metadata) {
		this.metadata = metadata;
	}

	public List<MicroserviceCreateTaskStepDto> getSteps() {
		return steps;
	}

	public void setSteps(List<MicroserviceCreateTaskStepDto> steps) {
		this.steps = steps;
	}

}
