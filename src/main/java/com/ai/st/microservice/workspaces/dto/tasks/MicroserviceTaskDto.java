package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TaskDto", description = "Task")
public class MicroserviceTaskDto implements Serializable {

	private static final long serialVersionUID = -572797587427843121L;

	@ApiModelProperty(required = true, notes = "Task ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Name")
	private String name;

	@ApiModelProperty(required = false, notes = "Description")
	private String description;

	@ApiModelProperty(required = false, notes = "Deadline")
	private Date deadline;

	@ApiModelProperty(required = true, notes = "Date creation")
	private Date createdAt;

	@ApiModelProperty(required = false, notes = "Closing Date")
	private Date closingDate;

	@ApiModelProperty(required = true, notes = "Task state")
	private MicroserviceTaskStateDto taskState;

	@ApiModelProperty(required = true, notes = "Members")
	private List<MicroserviceTaskMemberDto> members;

	@ApiModelProperty(required = true, notes = "Categories")
	private List<MicroserviceTaskCategoryDto> categories;

	@ApiModelProperty(required = true, notes = "Metadata")
	private List<MicroserviceTaskMetadataDto> metadata;

	@ApiModelProperty(required = true, notes = "Steps")
	private List<MicroserviceTaskStepDto> steps;

	public MicroserviceTaskDto() {
		this.members = new ArrayList<MicroserviceTaskMemberDto>();
		this.categories = new ArrayList<MicroserviceTaskCategoryDto>();
		this.metadata = new ArrayList<MicroserviceTaskMetadataDto>();
		this.steps = new ArrayList<MicroserviceTaskStepDto>();
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getClosingDate() {
		return closingDate;
	}

	public void setClosingDate(Date closingDate) {
		this.closingDate = closingDate;
	}

	public MicroserviceTaskStateDto getTaskState() {
		return taskState;
	}

	public void setTaskState(MicroserviceTaskStateDto taskState) {
		this.taskState = taskState;
	}

	public List<MicroserviceTaskMemberDto> getMembers() {
		return members;
	}

	public void setMembers(List<MicroserviceTaskMemberDto> members) {
		this.members = members;
	}

	public List<MicroserviceTaskCategoryDto> getCategories() {
		return categories;
	}

	public void setCategories(List<MicroserviceTaskCategoryDto> categories) {
		this.categories = categories;
	}

	public List<MicroserviceTaskMetadataDto> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<MicroserviceTaskMetadataDto> metadata) {
		this.metadata = metadata;
	}

	public List<MicroserviceTaskStepDto> getSteps() {
		return steps;
	}

	public void setSteps(List<MicroserviceTaskStepDto> steps) {
		this.steps = steps;
	}

}
