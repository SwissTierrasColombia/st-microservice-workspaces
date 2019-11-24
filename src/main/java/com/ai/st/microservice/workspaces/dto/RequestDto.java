package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RequestDto", description = "Request Dto")
public class RequestDto implements Serializable {

	private static final long serialVersionUID = -4732704090330832808L;

	@ApiModelProperty(required = true, notes = "Request ID")
	private Long id;

	public RequestDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
