package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MilestoneDto", description = "Milestone Dto")
public class MilestoneDto implements Serializable {

	private static final long serialVersionUID = 5839182543111562483L;

	@ApiModelProperty(required = true, notes = "Milestone ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Milestone name")
	private String name;

	public MilestoneDto() {

	}

	public MilestoneDto(Long id, String name) {
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
