package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;

public class MicroserviceCreateSupplyAttachmentDto implements Serializable {

	private static final long serialVersionUID = 335203584412319641L;

	private String data;
	private Long attachmentTypeId;

	public MicroserviceCreateSupplyAttachmentDto() {

	}

	public MicroserviceCreateSupplyAttachmentDto(String data, Long attachmentTypeId) {
		this.data = data;
		this.attachmentTypeId = attachmentTypeId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Long getAttachmentTypeId() {
		return attachmentTypeId;
	}

	public void setAttachmentTypeId(Long attachmentTypeId) {
		this.attachmentTypeId = attachmentTypeId;
	}

}
