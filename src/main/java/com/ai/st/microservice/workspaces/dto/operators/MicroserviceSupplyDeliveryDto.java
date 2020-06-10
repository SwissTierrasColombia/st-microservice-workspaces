package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;
import java.util.Date;

import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;

public class MicroserviceSupplyDeliveryDto implements Serializable {

	private static final long serialVersionUID = 6986943352552791077L;

	private Long id;
	private Date createdAt;
	private Boolean downloaded;
	private Date downloadedAt;
	private String observations;
	private Long supplyCode;
	private MicroserviceSupplyDto supply;
	private Long downloadedBy;
	private String downloadReportUrl;
	private MicroserviceUserDto userDownloaded;

	public MicroserviceSupplyDeliveryDto() {

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

	public Boolean getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(Boolean downloaded) {
		this.downloaded = downloaded;
	}

	public Date getDownloadedAt() {
		return downloadedAt;
	}

	public void setDownloadedAt(Date downloadedAt) {
		this.downloadedAt = downloadedAt;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Long getSupplyCode() {
		return supplyCode;
	}

	public void setSupplyCode(Long supplyCode) {
		this.supplyCode = supplyCode;
	}

	public MicroserviceSupplyDto getSupply() {
		return supply;
	}

	public void setSupply(MicroserviceSupplyDto supply) {
		this.supply = supply;
	}

	public Long getDownloadedBy() {
		return downloadedBy;
	}

	public void setDownloadedBy(Long downloadedBy) {
		this.downloadedBy = downloadedBy;
	}

	public String getDownloadReportUrl() {
		return downloadReportUrl;
	}

	public void setDownloadReportUrl(String downloadReportUrl) {
		this.downloadReportUrl = downloadReportUrl;
	}

	public MicroserviceUserDto getUserDownloaded() {
		return userDownloaded;
	}

	public void setUserDownloaded(MicroserviceUserDto userDownloaded) {
		this.userDownloaded = userDownloaded;
	}

}
