package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.Date;

import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WorkspaceOperatorDto")
public class WorkspaceOperatorDto implements Serializable {

	private static final long serialVersionUID = -1943824478121434815L;

	@ApiModelProperty(required = true, notes = "Workspace-Operator ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Date creation")
	private Date createdAt;

	@ApiModelProperty(required = true, notes = "Start date")
	private Date startDate;

	@ApiModelProperty(required = true, notes = "End date")
	private Date endDate;

	@ApiModelProperty(required = true, notes = "Number parcels expected")
	private Long numberParcelsExpected;

	@ApiModelProperty(required = false, notes = "Work area")
	private Double workArea;

	@ApiModelProperty(required = true, notes = "Operator Code")
	private Long operatorCode;

	@ApiModelProperty(required = false, notes = "Operator")
	private MicroserviceOperatorDto operator;

	@ApiModelProperty(required = true, notes = "Manager Code")
	private Long managerCode;

	@ApiModelProperty(required = false, notes = "Manager")
	private MicroserviceManagerDto manager;

	@ApiModelProperty(required = false, notes = "Observations")
	private String observations;

	public WorkspaceOperatorDto() {

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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getNumberParcelsExpected() {
		return numberParcelsExpected;
	}

	public void setNumberParcelsExpected(Long numberParcelsExpected) {
		this.numberParcelsExpected = numberParcelsExpected;
	}

	public Double getWorkArea() {
		return workArea;
	}

	public void setWorkArea(Double workArea) {
		this.workArea = workArea;
	}

	public Long getOperatorCode() {
		return operatorCode;
	}

	public void setOperatorCode(Long operatorCode) {
		this.operatorCode = operatorCode;
	}

	public MicroserviceOperatorDto getOperator() {
		return operator;
	}

	public void setOperator(MicroserviceOperatorDto operator) {
		this.operator = operator;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

	public MicroserviceManagerDto getManager() {
		return manager;
	}

	public void setManager(MicroserviceManagerDto manager) {
		this.manager = manager;
	}

}
