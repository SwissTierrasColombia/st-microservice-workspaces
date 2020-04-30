package com.ai.st.microservice.workspaces.dto.managers;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

public class MicroserviceUpdateManagerDto implements Serializable {

	private static final long serialVersionUID = 8630363838327832667L;
	
	private Long id;

	private String name;

	private String taxIdentificationNumber;

	public MicroserviceUpdateManagerDto() {

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
