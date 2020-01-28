package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceCreateSupplyDto implements Serializable {

	private static final long serialVersionUID = 7367459622173301312L;
	private String url;
	private String municipalityCode;
	private String observations;
	private Long typeSupplyCode;
	private Long requestCode;
	private List<MicroserviceCreateSupplyOwnerDto> owners;
	private List<String> urlsDocumentaryRepository;

	public MicroserviceCreateSupplyDto() {
		this.owners = new ArrayList<MicroserviceCreateSupplyOwnerDto>();
		this.urlsDocumentaryRepository = new ArrayList<String>();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public List<MicroserviceCreateSupplyOwnerDto> getOwners() {
		return owners;
	}

	public void setOwners(List<MicroserviceCreateSupplyOwnerDto> owners) {
		this.owners = owners;
	}

	public List<String> getUrlsDocumentaryRepository() {
		return urlsDocumentaryRepository;
	}

	public void setUrlsDocumentaryRepository(List<String> urlsDocumentaryRepository) {
		this.urlsDocumentaryRepository = urlsDocumentaryRepository;
	}

	public Long getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(Long requestCode) {
		this.requestCode = requestCode;
	}

}
