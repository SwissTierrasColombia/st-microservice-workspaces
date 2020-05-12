package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateProviderDto", description = "Create Provider Dto")
public class MicroserviceCreateProviderDto implements Serializable {

	private static final long serialVersionUID = 6515735489433109150L;

	@ApiModelProperty(required = true, notes = "Provider name")
	private String name;

	@ApiModelProperty(required = true, notes = "Provider tax identification number")
	private String taxIdentificationNumber;

	@ApiModelProperty(required = true, notes = "Provider Category ID")
	private Long providerCategoryId;

	public MicroserviceCreateProviderDto() {

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

	public Long getProviderCategoryId() {
		return providerCategoryId;
	}

	public void setProviderCategoryId(Long providerCategoryId) {
		this.providerCategoryId = providerCategoryId;
	}

}
