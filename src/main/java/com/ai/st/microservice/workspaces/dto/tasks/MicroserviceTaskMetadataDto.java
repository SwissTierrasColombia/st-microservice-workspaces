package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TaskMedataDto", description = "Task Metadata")
public class MicroserviceTaskMetadataDto implements Serializable {

	private static final long serialVersionUID = 5451686139173510244L;

	@ApiModelProperty(required = true, notes = "Id")
	private Long id;

	@ApiModelProperty(required = true, notes = "Key")
	private String key;

	@ApiModelProperty(required = true, notes = "Value")
	private String value;

	public MicroserviceTaskMetadataDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
