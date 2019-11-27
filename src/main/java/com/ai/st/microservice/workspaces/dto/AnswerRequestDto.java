package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "AnswerRequestDto", description = "Answer Request Dto")
public class AnswerRequestDto implements Serializable {

	private static final long serialVersionUID = 177256820188869027L;

	@ApiModelProperty(required = true, notes = "Type Supply")
	private Long typeSupplyId;

	@ApiModelProperty(required = false, notes = "Justification")
	private String justification;

	@ApiModelProperty(required = false, notes = "Url")
	private String url;

	public AnswerRequestDto() {

	}

	public Long getTypeSupplyId() {
		return typeSupplyId;
	}

	public void setTypeSupplyId(Long typeSupplyId) {
		this.typeSupplyId = typeSupplyId;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
