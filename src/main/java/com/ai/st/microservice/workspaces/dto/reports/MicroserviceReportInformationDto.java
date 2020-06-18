package com.ai.st.microservice.workspaces.dto.reports;

import java.io.Serializable;

public class MicroserviceReportInformationDto implements Serializable {

	private static final long serialVersionUID = -2036372078525852112L;
	private String urlReport;
	private Boolean reportGenerated;

	public MicroserviceReportInformationDto() {

	}

	public MicroserviceReportInformationDto(String urlReport, Boolean reportGenerated) {
		this.urlReport = urlReport;
		this.reportGenerated = reportGenerated;
	}

	public String getUrlReport() {
		return urlReport;
	}

	public void setUrlReport(String urlReport) {
		this.urlReport = urlReport;
	}

	public Boolean getReportGenerated() {
		return reportGenerated;
	}

	public void setReportGenerated(Boolean reportGenerated) {
		this.reportGenerated = reportGenerated;
	}

}
