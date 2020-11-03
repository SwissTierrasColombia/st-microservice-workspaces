package com.ai.st.microservice.workspaces.dto.providers;

import java.util.Date;

public class MicroserviceProviderDto {

	private Long id;
	private String name;
	private String alias;
	private String taxIdentificationNumber;
	private Date createdAt;
	private MicroserviceProviderCategoryDto providerCategory;

	public MicroserviceProviderDto() {

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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public MicroserviceProviderCategoryDto getProviderCategory() {
		return providerCategory;
	}

	public void setProviderCategory(MicroserviceProviderCategoryDto providerCategory) {
		this.providerCategory = providerCategory;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
