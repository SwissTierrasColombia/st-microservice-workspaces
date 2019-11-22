package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "StateDto", description = "State Dto")
public class StateDto implements Serializable {

	private static final long serialVersionUID = 65971403287652692L;

	@ApiModelProperty(required = true, notes = "State ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "State name")
	private String name;

	@ApiModelProperty(required = true, notes = "State description")
	private String description;

	public StateDto() {

	}

	public StateDto(Long id, String name, String description) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
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

}
