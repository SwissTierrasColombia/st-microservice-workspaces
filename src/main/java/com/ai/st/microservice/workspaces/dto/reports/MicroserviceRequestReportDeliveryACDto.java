package com.ai.st.microservice.workspaces.dto.reports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceRequestReportDeliveryACDto extends MicroserviceReportRequestDto implements Serializable {

	private static final long serialVersionUID = -112754967726891284L;

	private List<MicroserviceSupplyACDto> supplies;
	private String managerName;
	private String municipalityCode;
	private String municipalityName;
	private String departmentName;
	private String createdAt;

	public MicroserviceRequestReportDeliveryACDto() {
		this.supplies = new ArrayList<MicroserviceSupplyACDto>();
	}

	public List<MicroserviceSupplyACDto> getSupplies() {
		return supplies;
	}

	public void setSupplies(List<MicroserviceSupplyACDto> supplies) {
		this.supplies = supplies;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
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

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

}