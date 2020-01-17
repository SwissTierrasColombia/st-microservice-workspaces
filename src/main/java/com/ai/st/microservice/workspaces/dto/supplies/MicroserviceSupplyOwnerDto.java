package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceSupplyOwnerDto implements Serializable {

	private static final long serialVersionUID = -7648793015124385968L;

	private Long id;
	private Date createdAt;
	private Long ownerCode;
	private String ownerType;

	public MicroserviceSupplyOwnerDto() {

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

	public Long getOwnerCode() {
		return ownerCode;
	}

	public void setOwnerCode(Long ownerCode) {
		this.ownerCode = ownerCode;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

}
