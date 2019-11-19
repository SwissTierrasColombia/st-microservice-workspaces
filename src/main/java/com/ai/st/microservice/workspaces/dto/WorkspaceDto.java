package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WorkspaceDto", description = "Workspace Dto")
public class WorkspaceDto implements Serializable {

	private static final long serialVersionUID = 3626653639569669214L;

	@ApiModelProperty(required = true, notes = "Workspace ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Is active?")
	private Boolean isActive;

	@ApiModelProperty(required = true, notes = "Municipality area")
	private Double municipalityArea;

	@ApiModelProperty(required = true, notes = "Number alphanumeric parcerls")
	private Long numberAlphanumericParcels;

	@ApiModelProperty(required = true, notes = "Version")
	private Long version;

	@ApiModelProperty(required = true, notes = "Manager Code")
	private Long managerCode;

	@ApiModelProperty(required = true, notes = "Observations")
	private String observations;

	@ApiModelProperty(required = true, notes = "Date creation")
	private Date createdAt;

	@ApiModelProperty(required = true, notes = "Start date")
	private Date startDate;

	@ApiModelProperty(required = false, notes = "Manager")
	private ManagerDto manager;

	public WorkspaceDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Double getMunicipalityArea() {
		return municipalityArea;
	}

	public void setMunicipalityArea(Double municipalityArea) {
		this.municipalityArea = municipalityArea;
	}

	public Long getNumberAlphanumericParcels() {
		return numberAlphanumericParcels;
	}

	public void setNumberAlphanumericParcels(Long numberAlphanumericParcels) {
		this.numberAlphanumericParcels = numberAlphanumericParcels;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public ManagerDto getManager() {
		return manager;
	}

	public void setManager(ManagerDto manager) {
		this.manager = manager;
	}

}
