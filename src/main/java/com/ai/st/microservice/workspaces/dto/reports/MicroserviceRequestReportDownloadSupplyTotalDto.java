package com.ai.st.microservice.workspaces.dto.reports;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "RequestReportDownloadSupplyTotalDto")
public class MicroserviceRequestReportDownloadSupplyTotalDto extends MicroserviceReportRequestDto
		implements Serializable {

	private static final long serialVersionUID = -112754967726891284L;

	private String title;

	public MicroserviceRequestReportDownloadSupplyTotalDto() {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}