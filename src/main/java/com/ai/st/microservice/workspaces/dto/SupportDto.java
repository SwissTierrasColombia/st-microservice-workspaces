package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "SupportDto", description = "Support Dto")
public class SupportDto implements Serializable {

	private static final long serialVersionUID = -1252120303642259625L;

	@ApiModelProperty(required = true, notes = "Workspace ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Date creation")
	private Date createdAt;

	@ApiModelProperty(required = true, notes = "Url")
	private String urlDocumentaryRepository;

	@ApiModelProperty(required = true, notes = "Workspace")
	private WorkspaceDto workspace;

	@ApiModelProperty(required = true, notes = "Milestone")
	private MilestoneDto milestone;

	public SupportDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getUrlDocumentaryRepository() {
		return urlDocumentaryRepository;
	}

	public void setUrlDocumentaryRepository(String urlDocumentaryRepository) {
		this.urlDocumentaryRepository = urlDocumentaryRepository;
	}

	public WorkspaceDto getWorkspace() {
		return workspace;
	}

	public void setWorkspace(WorkspaceDto workspace) {
		this.workspace = workspace;
	}

	public MilestoneDto getMilestone() {
		return milestone;
	}

	public void setMilestone(MilestoneDto milestone) {
		this.milestone = milestone;
	}

}
