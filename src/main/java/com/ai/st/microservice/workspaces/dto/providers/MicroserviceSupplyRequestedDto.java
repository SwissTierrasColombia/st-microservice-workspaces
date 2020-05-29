package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.Date;

import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;

public class MicroserviceSupplyRequestedDto implements Serializable {

	private static final long serialVersionUID = -5639936149692833884L;

	private Long id;
	private String description;
	private MicroserviceTypeSupplyDto typeSupply;
	private Date createdAt;
	private Boolean delivered;
	private Date deliveredAt;
	private String justification;
	private MicroserviceSupplyRequestedStateDto state;
	private String modelVersion;
	private Boolean canUpload;
	private Long deliveredBy;
	private MicroserviceUserDto userDeliveryBy;
	private String url;
	private String observations;
	private String ftp;

	public MicroserviceSupplyRequestedDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public MicroserviceTypeSupplyDto getTypeSupply() {
		return typeSupply;
	}

	public void setTypeSupply(MicroserviceTypeSupplyDto typeSupply) {
		this.typeSupply = typeSupply;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Boolean getDelivered() {
		return delivered;
	}

	public void setDelivered(Boolean delivered) {
		this.delivered = delivered;
	}

	public Date getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(Date deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public MicroserviceSupplyRequestedStateDto getState() {
		return state;
	}

	public void setState(MicroserviceSupplyRequestedStateDto state) {
		this.state = state;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	public Boolean getCanUpload() {
		return canUpload;
	}

	public void setCanUpload(Boolean canUpload) {
		this.canUpload = canUpload;
	}

	public Long getDeliveredBy() {
		return deliveredBy;
	}

	public void setDeliveredBy(Long deliveredBy) {
		this.deliveredBy = deliveredBy;
	}

	public MicroserviceUserDto getUserDeliveryBy() {
		return userDeliveryBy;
	}

	public void setUserDeliveryBy(MicroserviceUserDto userDeliveryBy) {
		this.userDeliveryBy = userDeliveryBy;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public String getFtp() {
		return ftp;
	}

	public void setFtp(String ftp) {
		this.ftp = ftp;
	}

}
