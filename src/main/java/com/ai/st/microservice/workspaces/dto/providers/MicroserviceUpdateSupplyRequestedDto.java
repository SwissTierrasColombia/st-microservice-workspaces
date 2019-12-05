package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceUpdateSupplyRequestedDto implements Serializable {

	private static final long serialVersionUID = -1302858811309199472L;

	private Boolean delivered;
	private String justification;

	public MicroserviceUpdateSupplyRequestedDto() {

	}

	public Boolean getDelivered() {
		return delivered;
	}

	public void setDelivered(Boolean delivered) {
		this.delivered = delivered;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

}
