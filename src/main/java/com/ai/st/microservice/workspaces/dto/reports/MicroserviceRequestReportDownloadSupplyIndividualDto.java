package com.ai.st.microservice.workspaces.dto.reports;

import java.io.Serializable;

public class MicroserviceRequestReportDownloadSupplyIndividualDto extends MicroserviceReportRequestDto implements Serializable {

	private static final long serialVersionUID = -112754967726891284L;

	private String title;

	public MicroserviceRequestReportDownloadSupplyIndividualDto() {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}