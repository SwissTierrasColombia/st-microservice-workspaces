package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "IntegrationStatDto", description = "Integration Stat Dto")
public class IntegrationStatDto implements Serializable {

	private static final long serialVersionUID = 8551544715165376269L;

	@ApiModelProperty(required = true, notes = "Integration Stat ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "ANT records number")
	private Long antRecordsNumber;

	@ApiModelProperty(required = true, notes = "Cadastre records number")
	private Long cadastreRecordsNumber;

	@ApiModelProperty(required = true, notes = "SNR records number")
	private Long snrRecordsNumber;

	@ApiModelProperty(required = true, notes = "Percentage (Match)")
	private Double percentage;

	@ApiModelProperty(required = true, notes = "Created at")
	private Date createdAt;

	public IntegrationStatDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAntRecordsNumber() {
		return antRecordsNumber;
	}

	public void setAntRecordsNumber(Long antRecordsNumber) {
		this.antRecordsNumber = antRecordsNumber;
	}

	public Long getCadastreRecordsNumber() {
		return cadastreRecordsNumber;
	}

	public void setCadastreRecordsNumber(Long cadastreRecordsNumber) {
		this.cadastreRecordsNumber = cadastreRecordsNumber;
	}

	public Long getSnrRecordsNumber() {
		return snrRecordsNumber;
	}

	public void setSnrRecordsNumber(Long snrRecordsNumber) {
		this.snrRecordsNumber = snrRecordsNumber;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
