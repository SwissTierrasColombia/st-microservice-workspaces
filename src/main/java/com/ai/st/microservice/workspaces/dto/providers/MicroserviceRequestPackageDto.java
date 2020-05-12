package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceRequestPackageDto implements Serializable {

	private static final long serialVersionUID = -181941292488270615L;

	private String packageLabel;
	private List<MicroserviceRequestDto> requests;

	public MicroserviceRequestPackageDto() {
		this.requests = new ArrayList<>();
	}

	public List<MicroserviceRequestDto> getRequests() {
		return requests;
	}

	public void setRequests(List<MicroserviceRequestDto> requests) {
		this.requests = requests;
	}

	public String getPackageLabel() {
		return packageLabel;
	}

	public void setPackageLabel(String packageLabel) {
		this.packageLabel = packageLabel;
	}

}
