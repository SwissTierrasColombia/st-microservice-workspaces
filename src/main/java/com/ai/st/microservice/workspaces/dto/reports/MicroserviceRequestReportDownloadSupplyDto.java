package com.ai.st.microservice.workspaces.dto.reports;

import java.io.Serializable;
import java.util.List;

public class MicroserviceRequestReportDownloadSupplyDto extends MicroserviceReportRequestDto implements Serializable {

	private static final long serialVersionUID = -112754967726891284L;

	private List<MicroserviceDownloadedSupplyDto> supplies;
	private String deliveryId;
	private String dateDelivery;
	private String managerName;
	private String operatorName;
	private String municipalityCode;
	private String municipalityName;
	private String departmentName;
	private String observations;
	private String dateCreation;

	public MicroserviceRequestReportDownloadSupplyDto() {

	}

	public List<MicroserviceDownloadedSupplyDto> getSupplies() {
		return supplies;
	}

	public void setSupplies(List<MicroserviceDownloadedSupplyDto> supplies) {
		this.supplies = supplies;
	}

	public String getDeliveryId() {
		return deliveryId;
	}

	public void setDeliveryId(String deliveryId) {
		this.deliveryId = deliveryId;
	}

	public String getDateDelivery() {
		return dateDelivery;
	}

	public void setDateDelivery(String dateDelivery) {
		this.dateDelivery = dateDelivery;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getMunicipalityCode() {
		return municipalityCode;
	}

	public void setMunicipalityCode(String municipalityCode) {
		this.municipalityCode = municipalityCode;
	}

	public String getMunicipalityName() {
		return municipalityName;
	}

	public void setMunicipalityName(String municipalityName) {
		this.municipalityName = municipalityName;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public String getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(String dateCreation) {
		this.dateCreation = dateCreation;
	}

}