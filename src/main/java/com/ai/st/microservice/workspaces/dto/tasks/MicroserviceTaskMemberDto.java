package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;
import java.util.Date;

import com.ai.st.microservice.workspaces.dto.administration.UserDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TaskMemberDto", description = "Task Member")
public class MicroserviceTaskMemberDto implements Serializable {

	private static final long serialVersionUID = 7581006574042957605L;

	@ApiModelProperty(required = true, notes = "Task member ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Member code")
	private Long memberCode;

	@ApiModelProperty(required = true, notes = "Date creation")
	private Date createdAt;

	@ApiModelProperty(required = true, notes = "User")
	private UserDto user;

	public MicroserviceTaskMemberDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberCode() {
		return memberCode;
	}

	public void setMemberCode(Long memberCode) {
		this.memberCode = memberCode;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

}
