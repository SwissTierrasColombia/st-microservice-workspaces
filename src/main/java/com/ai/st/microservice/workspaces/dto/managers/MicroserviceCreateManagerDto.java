package com.ai.st.microservice.workspaces.dto.managers;

import java.io.Serializable;

public class MicroserviceCreateManagerDto implements Serializable {

	private static final long serialVersionUID = 8630363838327832666L;

	private String name;

	private String taxIdentificationNumber;

	public MicroserviceCreateManagerDto() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTaxIdentificationNumber() {
		return taxIdentificationNumber;
	}

	public void setTaxIdentificationNumber(String taxIdentificationNumber) {
		this.taxIdentificationNumber = taxIdentificationNumber;
	}

}
