package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateWorkspaceDto", description = "Create Workspace Dto")
public class CreateWorkspaceDto implements Serializable {

	private static final long serialVersionUID = -6853674829966701765L;

	@ApiModelProperty(required = true, notes = "Start date")
	private String startDate;

	@ApiModelProperty(required = true, notes = "End date")
	private String endDate;

	@ApiModelProperty(required = true, notes = "Manager Code")
	private Long managerCode;

	@ApiModelProperty(required = true, notes = "Observations")
	private String observations;

	@ApiModelProperty(required = false, notes = "Parcels number")
	private Long parcelsNumber;

	@ApiModelProperty(required = true, notes = "Municipality ID")
	private Long municipalityId;
	
	@ApiModelProperty(required = true, notes = "Support file")
	private MultipartFile supportFile;
	
	public CreateWorkspaceDto() {

	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Long getParcelsNumber() {
		return parcelsNumber;
	}

	public void setParcelsNumber(Long parcelsNumber) {
		this.parcelsNumber = parcelsNumber;
	}

	public Long getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(Long municipalityId) {
		this.municipalityId = municipalityId;
	}

	public MultipartFile getSupportFile() {
		return supportFile;
	}

	public void setSupportFile(MultipartFile supportFile) {
		this.supportFile = supportFile;
	}

}
