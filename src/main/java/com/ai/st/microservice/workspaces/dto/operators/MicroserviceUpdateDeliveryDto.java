package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;

public class MicroserviceUpdateDeliveryDto implements Serializable {

	private static final long serialVersionUID = -3896720955663959193L;

	private String reportUrl;

	public MicroserviceUpdateDeliveryDto() {

	}

	public String getReportUrl() {
		return reportUrl;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

}
