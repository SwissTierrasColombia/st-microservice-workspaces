package com.ai.st.microservice.workspaces.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "workspaces", schema = "workspaces")
public class WorkspaceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "municipality_area", precision = 10, scale = 2, nullable = true)
	private Double municipalityArea;

	@Column(name = "number_alphanumeric_parcels", nullable = true)
	private Long numberAlphanumericParcels;

	@Column(name = "version", nullable = false)
	private Long version;

	@Column(name = "manager_code", nullable = false)
	private Long managerCode;

	@Column(name = "observations", nullable = false, length = 255)
	private String observations;

	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name = "updated_at", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date udpatedAt;

	@Column(name = "start_date", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date startDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipality_id", referencedColumnName = "id", nullable = false)
	private MunicipalityEntity municipality;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id", referencedColumnName = "id", nullable = true)
	private WorkspaceEntity workspace;

	@OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
	private List<SupportEntity> supports = new ArrayList<SupportEntity>();

	@OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<WorkspaceOperatorEntity> operators = new ArrayList<WorkspaceOperatorEntity>();

	public WorkspaceEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public MunicipalityEntity getMunicipality() {
		return municipality;
	}

	public void setMunicipality(MunicipalityEntity municipality) {
		this.municipality = municipality;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public List<SupportEntity> getSupports() {
		return supports;
	}

	public void setSupports(List<SupportEntity> supports) {
		this.supports = supports;
	}

	public Double getMunicipalityArea() {
		return municipalityArea;
	}

	public void setMunicipalityArea(Double municipalityArea) {
		this.municipalityArea = municipalityArea;
	}

	public Long getNumberAlphanumericParcels() {
		return numberAlphanumericParcels;
	}

	public void setNumberAlphanumericParcels(Long numberAlphanumericParcels) {
		this.numberAlphanumericParcels = numberAlphanumericParcels;
	}

	public List<WorkspaceOperatorEntity> getOperators() {
		return operators;
	}

	public void setOperators(List<WorkspaceOperatorEntity> operators) {
		this.operators = operators;
	}

	public Date getUdpatedAt() {
		return udpatedAt;
	}

	public void setUdpatedAt(Date udpatedAt) {
		this.udpatedAt = udpatedAt;
	}

	public WorkspaceEntity getWorkspace() {
		return workspace;
	}

	public void setWorkspace(WorkspaceEntity workspace) {
		this.workspace = workspace;
	}

}
