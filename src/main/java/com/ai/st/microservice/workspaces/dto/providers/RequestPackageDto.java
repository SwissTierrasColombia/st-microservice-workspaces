package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RequestPackageDto implements Serializable {

	private static final long serialVersionUID = -181941292488270615L;

	private String packageLabel;
	private List<CustomRequestDto> requests;

	public RequestPackageDto() {
		this.requests = new ArrayList<>();
	}

	public List<CustomRequestDto> getRequests() {
		return requests;
	}

	public void setRequests(List<CustomRequestDto> requests) {
		this.requests = requests;
	}

	public String getPackageLabel() {
		return packageLabel;
	}

	public void setPackageLabel(String packageLabel) {
		this.packageLabel = packageLabel;
	}

}
