package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyDto;

public class MicroserviceSupplyDto implements Serializable {

	private static final long serialVersionUID = 8259586718340595896L;

	private Long id;
	private Date createdAt;
	private String url;
	private MicroserviceSupplyStateDto state;
	private String municipalityCode;
	private String observations;
	private Long typeSupplyCode;
	private List<MicroserviceSupplyOwnerDto> owners;
	private List<MicroserviceSupplyAttachmentDto> attachments;
	private MicroserviceTypeSupplyDto typeSupply;

	public MicroserviceSupplyDto() {
		this.owners = new ArrayList<MicroserviceSupplyOwnerDto>();
		this.attachments = new ArrayList<MicroserviceSupplyAttachmentDto>();
		this.typeSupply = null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public MicroserviceSupplyStateDto getState() {
		return state;
	}

	public void setState(MicroserviceSupplyStateDto state) {
		this.state = state;
	}

	public String getMunicipalityCode() {
		return municipalityCode;
	}

	public void setMunicipalityCode(String municipalityCode) {
		this.municipalityCode = municipalityCode;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Long getTypeSupplyCode() {
		return typeSupplyCode;
	}

	public void setTypeSupplyCode(Long typeSupplyCode) {
		this.typeSupplyCode = typeSupplyCode;
	}

	public List<MicroserviceSupplyOwnerDto> getOwners() {
		return owners;
	}

	public void setOwners(List<MicroserviceSupplyOwnerDto> owners) {
		this.owners = owners;
	}

	public List<MicroserviceSupplyAttachmentDto> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<MicroserviceSupplyAttachmentDto> attachments) {
		this.attachments = attachments;
	}

	public MicroserviceTypeSupplyDto getTypeSupply() {
		return typeSupply;
	}

	public void setTypeSupply(MicroserviceTypeSupplyDto typeSupply) {
		this.typeSupply = typeSupply;
	}

}
