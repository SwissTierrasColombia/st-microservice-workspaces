package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceCreateAttachmentDto implements Serializable {

	private static final long serialVersionUID = -5683375812731682537L;

	private Long boundaryId;
	private String fileUrl;
	private Long createdBy;

	public MicroserviceCreateAttachmentDto() {
	}

	public Long getBoundaryId() {
		return boundaryId;
	}

	public void setBoundaryId(Long boundaryId) {
		this.boundaryId = boundaryId;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

}
