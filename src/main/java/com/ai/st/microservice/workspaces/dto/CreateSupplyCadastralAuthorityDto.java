package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreatePetitionDto")
public class CreateSupplyCadastralAuthorityDto implements Serializable {

	private static final long serialVersionUID = 6161636556899381195L;

	@ApiModelProperty(required = true, notes = "Attachment Type ID")
	private Long attachmentTypeId;

	@ApiModelProperty(required = false, notes = "FTP")
	private String ftp;

	@ApiModelProperty(required = false, notes = "Observations")
	private String observations;

	public CreateSupplyCadastralAuthorityDto() {

	}

	public Long getAttachmentTypeId() {
		return attachmentTypeId;
	}

	public void setAttachmentTypeId(Long attachmentTypeId) {
		this.attachmentTypeId = attachmentTypeId;
	}

	public String getFtp() {
		return ftp;
	}

	public void setFtp(String ftp) {
		this.ftp = ftp;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

}
