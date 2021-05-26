package com.ai.st.microservice.workspaces.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name = "updated_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipality_id", referencedColumnName = "id", nullable = false)
	private MunicipalityEntity municipality;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id", referencedColumnName = "id")
	private WorkspaceEntity workspace;

	@OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
	private List<WorkspaceOperatorEntity> operators = new ArrayList<>();

	@OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
	private List<WorkspaceManagerEntity> managers = new ArrayList<>();

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

	public List<WorkspaceOperatorEntity> getOperators() {
		return operators;
	}

	public void setOperators(List<WorkspaceOperatorEntity> operators) {
		this.operators = operators;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public WorkspaceEntity getWorkspace() {
		return workspace;
	}

	public void setWorkspace(WorkspaceEntity workspace) {
		this.workspace = workspace;
	}

	public List<WorkspaceManagerEntity> getManagers() {
		return managers;
	}

	public void setManagers(List<WorkspaceManagerEntity> managers) {
		this.managers = managers;
	}

}
