package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;

public class MicroserviceUpdateDeliveredSupplyDto implements Serializable {

	private static final long serialVersionUID = 6618248721399600770L;

	private Boolean downloaded;
	private String observations;
	private String reportUrl;
	private Long downloadedBy;

	public MicroserviceUpdateDeliveredSupplyDto() {

	}

	public Boolean getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(Boolean downloaded) {
		this.downloaded = downloaded;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public String getReportUrl() {
		return reportUrl;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

	public Long getDownloadedBy() {
		return downloadedBy;
	}

	public void setDownloadedBy(Long downloadedBy) {
		this.downloadedBy = downloadedBy;
	}

}
