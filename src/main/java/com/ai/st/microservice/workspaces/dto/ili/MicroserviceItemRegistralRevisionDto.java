package com.ai.st.microservice.workspaces.dto.ili;

import java.io.Serializable;

public class MicroserviceItemRegistralRevisionDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String newFmi;
	private String oldFmi;
	private String orip;
	private String realEstateRegistration;
	private String nomenclature;
	private String boundarySpace;
	private Long fileId;
	private Long boundaryId;
	private String issuingCity;
	private String issuingEntity;
	private String documentDate;
	private String documentNumber;
	private String documentType;

	public MicroserviceItemRegistralRevisionDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNewFmi() {
		return newFmi;
	}

	public void setNewFmi(String newFmi) {
		this.newFmi = newFmi;
	}

	public String getOldFmi() {
		return oldFmi;
	}

	public void setOldFmi(String oldFmi) {
		this.oldFmi = oldFmi;
	}

	public String getOrip() {
		return orip;
	}

	public void setOrip(String orip) {
		this.orip = orip;
	}

	public String getRealEstateRegistration() {
		return realEstateRegistration;
	}

	public void setRealEstateRegistration(String realEstateRegistration) {
		this.realEstateRegistration = realEstateRegistration;
	}

	public String getNomenclature() {
		return nomenclature;
	}

	public void setNomenclature(String nomenclature) {
		this.nomenclature = nomenclature;
	}

	public String getBoundarySpace() {
		return boundarySpace;
	}

	public void setBoundarySpace(String boundarySpace) {
		this.boundarySpace = boundarySpace;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public Long getBoundaryId() {
		return boundaryId;
	}

	public void setBoundaryId(Long boundaryId) {
		this.boundaryId = boundaryId;
	}

	public String getIssuingCity() {
		return issuingCity;
	}

	public void setIssuingCity(String issuingCity) {
		this.issuingCity = issuingCity;
	}

	public String getIssuingEntity() {
		return issuingEntity;
	}

	public void setIssuingEntity(String issuingEntity) {
		this.issuingEntity = issuingEntity;
	}

	public String getDocumentDate() {
		return documentDate;
	}

	public void setDocumentDate(String documentDate) {
		this.documentDate = documentDate;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

}
