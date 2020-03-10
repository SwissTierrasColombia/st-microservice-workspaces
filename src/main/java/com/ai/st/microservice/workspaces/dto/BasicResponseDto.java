package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ErrorDto", description = "Error Dto")
public class BasicResponseDto implements Serializable {

	private static final long serialVersionUID = 5898345343466699696L;

	@ApiModelProperty(required = true, notes = "Message error")
	private String message;

	@ApiModelProperty(required = true, notes = "Code error")
	private Integer code;

	public BasicResponseDto() {

	}

	public BasicResponseDto(String message, Integer code) {
		super();
		this.message = message;
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

}
