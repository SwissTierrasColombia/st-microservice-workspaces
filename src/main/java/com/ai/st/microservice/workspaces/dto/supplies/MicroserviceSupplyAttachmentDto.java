package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceSupplyAttachmentDto implements Serializable {

	private static final long serialVersionUID = 7112301654715963689L;

	private Long id;
	private Date createdAt;
	private String urlDocumentaryRepository;

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

	public String getUrlDocumentaryRepository() {
		return urlDocumentaryRepository;
	}

	public void setUrlDocumentaryRepository(String urlDocumentaryRepository) {
		this.urlDocumentaryRepository = urlDocumentaryRepository;
	}

}
