package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WorkspaceDto", description = "Workspace Dto")
public class WorkspaceDto implements Serializable {

	private static final long serialVersionUID = 3626653639569669214L;

	@ApiModelProperty(required = true, notes = "Workspace ID")
	private Long id;

	public WorkspaceDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
