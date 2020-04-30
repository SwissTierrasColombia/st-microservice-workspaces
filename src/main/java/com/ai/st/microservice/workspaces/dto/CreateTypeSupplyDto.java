package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ai.st.microservice.workspaces.dto.ExtensionDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateTypeSupplyDto", description = "Create Type Supply Dto")
public class CreateTypeSupplyDto implements Serializable {

	private static final long serialVersionUID = 6515735489433109152L;

	@ApiModelProperty(required = true, notes = "Description")
	private String description;

	@ApiModelProperty(required = true, notes = "Metadata is required ?")
	private Boolean metadataRequired;

	@ApiModelProperty(required = true, notes = "Model is required ?")
	private Boolean modelRequired;

	@ApiModelProperty(required = true, notes = "Type supply name")
	private String name;

	@ApiModelProperty(required = true, notes = "Provider profile")
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


	public CreateTypeSupplyDto() {
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
