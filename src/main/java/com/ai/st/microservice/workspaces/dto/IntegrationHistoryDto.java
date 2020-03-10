package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "IntegrationHistoryDto", description = "Integration History Dto")
public class IntegrationHistoryDto implements Serializable {

	private static final long serialVersionUID = -6132580187596130900L;

	@ApiModelProperty(required = true, notes = "Integration ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Created at")
	private Date createdAt;

	@ApiModelProperty(required = true, notes = "State")
	private IntegrationStateDto state;

	@ApiModelProperty(required = true, notes = "User name")
	private String userName;

	public IntegrationHistoryDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IntegrationStateDto getState() {
		return state;
	}

	public void setState(IntegrationStateDto state) {
		this.state = state;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
