package com.ai.st.microservice.workspaces.dto.reports;

import java.io.Serializable;

public class MicroserviceDownloadedSupplyDto implements Serializable {

	private static final long serialVersionUID = -3681367307489938097L;

	private String supplyName;
	private String downloadedAt;
	private String downloadedBy;
	private String providerName;

	public MicroserviceDownloadedSupplyDto() {

	}

	public MicroserviceDownloadedSupplyDto(String supplyName, String downloadedAt, String downloadedBy,
			String providerName) {
		super();
		this.supplyName = supplyName;
		this.downloadedAt = downloadedAt;
		this.downloadedBy = downloadedBy;
		this.providerName = providerName;
	}

	public String getSupplyName() {
		return supplyName;
	}

	public void setSupplyName(String supplyName) {
		this.supplyName = supplyName;
	}

	public String getDownloadedAt() {
		return downloadedAt;
	}

	public void setDownloadedAt(String downloadedAt) {
		this.downloadedAt = downloadedAt;
	}

	public String getDownloadedBy() {
		return downloadedBy;
	}

	public void setDownloadedBy(String downloadedBy) {
		this.downloadedBy = downloadedBy;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

}
