package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceSupplyAttachmentDto implements Serializable {

	private static final long serialVersionUID = 7112301654715963689L;

	private Long id;
	private Date createdAt;
	private String data;
	private MicroserviceSupplyAttachmentTypeDto attachmentType;

	public MicroserviceSupplyAttachmentDto() {

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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public MicroserviceSupplyAttachmentTypeDto getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(MicroserviceSupplyAttachmentTypeDto attachmentType) {
		this.attachmentType = attachmentType;
	}

}
