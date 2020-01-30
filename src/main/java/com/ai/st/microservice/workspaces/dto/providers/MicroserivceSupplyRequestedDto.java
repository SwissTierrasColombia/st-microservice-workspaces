package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.Date;

public class MicroserivceSupplyRequestedDto implements Serializable {

	private static final long serialVersionUID = -5639936149692833884L;

	private Long id;
	private String description;
	private MicroserviceTypeSupplyDto typeSupply;
	private Date createdAt;
	private Boolean delivered;
	private Date deliveredAt;
	private String justification;
	private MicroserviceSupplyRequestedStateDto state;

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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Boolean getDelivered() {
		return delivered;
	}

	public void setDelivered(Boolean delivered) {
		this.delivered = delivered;
	}

	public Date getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(Date deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public MicroserviceSupplyRequestedStateDto getState() {
		return state;
	}

	public void setState(MicroserviceSupplyRequestedStateDto state) {
		this.state = state;
	}

}
