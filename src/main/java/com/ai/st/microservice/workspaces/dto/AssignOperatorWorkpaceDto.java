package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "AssignOperatorWorkpaceDto", description = "Assign Operator Dto")
public class AssignOperatorWorkpaceDto implements Serializable {

	private static final long serialVersionUID = -4217826518362864745L;

	@ApiModelProperty(required = true, notes = "Start date")
	private String startDate;

	@ApiModelProperty(required = true, notes = "End date")
	private String endDate;

	@ApiModelProperty(required = false, notes = "Number parcels expected")
	private Long numberParcelsExpected;

	@ApiModelProperty(required = true, notes = "Operator Code")
	private Long operatorCode;

	@ApiModelProperty(required = false, notes = "Work area")
	private Double workArea;

	@ApiModelProperty(required = true, notes = "Support file")
	private MultipartFile supportFile;

	public AssignOperatorWorkpaceDto() {

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

	public Long getNumberParcelsExpected() {
		return numberParcelsExpected;
	}

	public void setNumberParcelsExpected(Long numberParcelsExpected) {
		this.numberParcelsExpected = numberParcelsExpected;
	}

	public Long getOperatorCode() {
		return operatorCode;
	}

	public void setOperatorCode(Long operatorCode) {
		this.operatorCode = operatorCode;
	}

	public Double getWorkArea() {
		return workArea;
	}

	public void setWorkArea(Double workArea) {
		this.workArea = workArea;
	}

	public MultipartFile getSupportFile() {
		return supportFile;
	}

	public void setSupportFile(MultipartFile supportFile) {
		this.supportFile = supportFile;
	}

}
