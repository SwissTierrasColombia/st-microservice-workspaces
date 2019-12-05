package com.ai.st.microservice.workspaces.dto.filemanager;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "FileManagerResponseDto", description = "File Manager Response Dto")
public class MicroserviceFilemanagerResponseDto implements Serializable {

	private static final long serialVersionUID = 7997850542001553998L;

	@ApiModelProperty(required = true, notes = "Status")
	private Boolean status;

	@ApiModelProperty(required = true, notes = "Url")
	private String url;

	public MicroserviceFilemanagerResponseDto() {

	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
