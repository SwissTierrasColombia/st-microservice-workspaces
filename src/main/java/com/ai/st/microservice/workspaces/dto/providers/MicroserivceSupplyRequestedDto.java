package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserivceSupplyRequestedDto implements Serializable {

	private static final long serialVersionUID = -5639936149692833884L;

	private Long id;
	private String description;
	private MicroserviceTypeSupplyDto typeSupply;

	public MicroserivceSupplyRequestedDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public MicroserviceTypeSupplyDto getTypeSupply() {
		return typeSupply;
	}

	public void setTypeSupply(MicroserviceTypeSupplyDto typeSupply) {
		this.typeSupply = typeSupply;
	}

}
