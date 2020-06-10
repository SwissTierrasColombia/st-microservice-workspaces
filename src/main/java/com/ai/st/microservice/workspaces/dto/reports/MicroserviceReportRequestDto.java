package com.ai.st.microservice.workspaces.dto.reports;

public class MicroserviceReportRequestDto {
	
	private String namespace;
	private String filename;

	public MicroserviceReportRequestDto() {

	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
