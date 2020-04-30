package com.ai.st.microservice.workspaces.dto.managers;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ManagerDto", description = "Create Manger Dto")
public class MicroserviceCreateManagerDto implements Serializable {

	private static final long serialVersionUID = 8630363838327832666L;

	@ApiModelProperty(required = true, notes = "Manager name")
	private String name;

	@ApiModelProperty(required = true, notes = "Manager tax identification number")
	private String taxIdentificationNumber;

	public MicroserviceCreateManagerDto() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTaxIdentificationNumber() {
		return taxIdentificationNumber;
	}

	public void setTaxIdentificationNumber(String taxIdentificationNumber) {
		this.taxIdentificationNumber = taxIdentificationNumber;
	}

}
