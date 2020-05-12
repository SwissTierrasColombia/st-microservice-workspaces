package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "UpdateProviderDto", description = "Update Provider Dto")
public class MicroserviceUpdateProviderDto implements Serializable {

	private static final long serialVersionUID = 6515735489433109160L;
	
	@ApiModelProperty(required = true, notes = "Provider ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Provider name")
	private String name;

	@ApiModelProperty(required = true, notes = "Provider tax identification number")
	private String taxIdentificationNumber;

	@ApiModelProperty(required = true, notes = "Provider Category ID")
	private Long providerCategoryId;

	public MicroserviceUpdateProviderDto() {

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
