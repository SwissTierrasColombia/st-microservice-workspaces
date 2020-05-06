package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceCreateTypeSupplyDto implements Serializable {

	private static final long serialVersionUID = 6515735489433109152L;

	private String description;
	private Boolean metadataRequired;
	private Boolean modelRequired;
	private String name;
	private Long providerProfileId;
	private List<String> extensions;

	public List<String> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<String> extensions) {
		this.extensions = extensions;
	}

	public Boolean getModelRequired() {
		return modelRequired;
	}

	public void setModelRequired(Boolean modelRequired) {
		this.modelRequired = modelRequired;
	}

	public MicroserviceCreateTypeSupplyDto() {
		this.extensions = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getProviderProfileId() {
		return providerProfileId;
	}

	public void setProviderProfileId(Long providerProfileId) {
		this.providerProfileId = providerProfileId;
	}

	public Boolean getMetadataRequired() {
		return metadataRequired;
	}

	public void setMetadataRequired(Boolean metadataRequired) {
		this.metadataRequired = metadataRequired;
	}

}
