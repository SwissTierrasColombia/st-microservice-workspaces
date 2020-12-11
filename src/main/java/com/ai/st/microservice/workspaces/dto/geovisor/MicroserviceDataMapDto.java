package com.ai.st.microservice.workspaces.dto.geovisor;

import java.io.Serializable;

public class MicroserviceDataMapDto implements Serializable {

	private static final long serialVersionUID = 7795987778570923080L;

	private String data;

	public MicroserviceDataMapDto() {

	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
