package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MicroserviceTypeSupplyDto implements Serializable {

	private static final long serialVersionUID = 3977770440374512592L;

	private Long id;
	private Date createdAt;
	private String description;
	private Boolean metadataRequired;
	private Boolean modelRequired;
	private String name;
	private Boolean active;
	private MicroserviceProviderProfileDto providerProfile;
	private List<MicroserviceExtensionDto> extensions;
	private MicroserviceProviderDto provider;

	public MicroserviceTypeSupplyDto() {
		this.extensions = new ArrayList<MicroserviceExtensionDto>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getMetadataRequired() {
		return metadataRequired;
	}

	public void setMetadataRequired(Boolean metadataRequired) {
		this.metadataRequired = metadataRequired;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MicroserviceProviderProfileDto getProviderProfile() {
		return providerProfile;
	}

	public void setProviderProfile(MicroserviceProviderProfileDto providerProfile) {
		this.providerProfile = providerProfile;
	}

	public List<MicroserviceExtensionDto> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<MicroserviceExtensionDto> extensions) {
		this.extensions = extensions;
	}

	public MicroserviceProviderDto getProvider() {
		return provider;
	}

	public void setProvider(MicroserviceProviderDto provider) {
		this.provider = provider;
	}

	public Boolean getModelRequired() {
		return modelRequired;
	}

	public void setModelRequired(Boolean modelRequired) {
		this.modelRequired = modelRequired;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
