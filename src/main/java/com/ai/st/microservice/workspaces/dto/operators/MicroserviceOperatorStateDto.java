package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "OperatorStateDto", description = "Operator State Dto")
public class MicroserviceOperatorStateDto implements Serializable {

	private static final long serialVersionUID = 844681853006719507L;

	@ApiModelProperty(required = true, notes = "State ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "State name")
	private String name;

	public MicroserviceOperatorStateDto() {

	}

	public MicroserviceOperatorStateDto(Long id, String name) {
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
