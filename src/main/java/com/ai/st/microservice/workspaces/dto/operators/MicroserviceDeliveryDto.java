package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;

public class MicroserviceDeliveryDto implements Serializable {

	private static final long serialVersionUID = -5027313678039653625L;

	private Long id;
	private Date createdAt;
	private Long managerCode;
	private String municipalityCode;
	private Boolean isActive;
	private String observations;
	private MicroserviceOperatorDto operator;
	private List<MicroserviceSupplyDeliveryDto> supplies;
	private MicroserviceManagerDto manager;
	private MunicipalityDto municipality;
	private String downloadReportUrl;

	public MicroserviceDeliveryDto() {
		this.supplies = new ArrayList<>();
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

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

	public String getMunicipalityCode() {
		return municipalityCode;
	}

	public void setMunicipalityCode(String municipalityCode) {
		this.municipalityCode = municipalityCode;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public MicroserviceOperatorDto getOperator() {
		return operator;
	}

	public void setOperator(MicroserviceOperatorDto operator) {
		this.operator = operator;
	}

	public List<MicroserviceSupplyDeliveryDto> getSupplies() {
		return supplies;
	}

	public void setSupplies(List<MicroserviceSupplyDeliveryDto> supplies) {
		this.supplies = supplies;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public MicroserviceManagerDto getManager() {
		return manager;
	}

	public void setManager(MicroserviceManagerDto manager) {
		this.manager = manager;
	}

	public MunicipalityDto getMunicipality() {
		return municipality;
	}

	public void setMunicipality(MunicipalityDto municipality) {
		this.municipality = municipality;
	}

	public String getDownloadReportUrl() {
		return downloadReportUrl;
	}

	public void setDownloadReportUrl(String downloadReportUrl) {
		this.downloadReportUrl = downloadReportUrl;
	}

}
