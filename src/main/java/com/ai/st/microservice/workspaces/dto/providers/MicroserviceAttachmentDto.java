package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceAttachmentDto implements Serializable {

	private static final long serialVersionUID = 3495734410935760225L;

	private Long id;
	private MicroserviceSupplyRequestedDto supplyRequested;
	private String fileUrl;
	private Long boundaryId;
	private Date createdAt;
	private Long createdBy;

	public MicroserviceAttachmentDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MicroserviceSupplyRequestedDto getSupplyRequested() {
		return supplyRequested;
	}

	public void setSupplyRequested(MicroserviceSupplyRequestedDto supplyRequested) {
		this.supplyRequested = supplyRequested;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public Long getBoundaryId() {
		return boundaryId;
	}

	public void setBoundaryId(Long boundaryId) {
		this.boundaryId = boundaryId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

}
