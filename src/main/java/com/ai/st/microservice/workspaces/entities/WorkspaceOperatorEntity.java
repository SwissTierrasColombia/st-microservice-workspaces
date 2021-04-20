package com.ai.st.microservice.workspaces.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "workspace_operators", schema = "workspaces")
public class WorkspaceOperatorEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name = "start_date", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Column(name = "end_date", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@Column(name = "operator_code", nullable = false)
	private Long operatorCode;

	@Column(name = "manager_code", nullable = false)
	private Long managerCode;

	@Column(name = "number_parcels_expected", nullable = true)
	private Long numberParcelsExpected;

	@Column(name = "work_area", precision = 10, scale = 2, nullable = true)
	private Double workArea;

	@Column(name = "observations", nullable = true, length = 255)
	private String observations;

	@Column(name = "support", nullable = false, length = 1000)
	private String supportFile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id", referencedColumnName = "id", nullable = false)
	private WorkspaceEntity workspace;

	public WorkspaceOperatorEntity() {

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

	public Long getOperatorCode() {
		return operatorCode;
	}

	public void setOperatorCode(Long operatorCode) {
		this.operatorCode = operatorCode;
	}

	public WorkspaceEntity getWorkspace() {
		return workspace;
	}

	public void setWorkspace(WorkspaceEntity workspace) {
		this.workspace = workspace;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getNumberParcelsExpected() {
		return numberParcelsExpected;
	}

	public void setNumberParcelsExpected(Long numberParcelsExpected) {
		this.numberParcelsExpected = numberParcelsExpected;
	}

	public Double getWorkArea() {
		return workArea;
	}

	public void setWorkArea(Double workArea) {
		this.workArea = workArea;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public String getSupportFile() {
		return supportFile;
	}

	public void setSupportFile(String supportFile) {
		this.supportFile = supportFile;
	}

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

}
