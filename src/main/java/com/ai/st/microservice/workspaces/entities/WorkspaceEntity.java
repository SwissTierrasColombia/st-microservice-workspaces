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

	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name = "updated_at", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date udpatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipality_id", referencedColumnName = "id", nullable = false)
	private MunicipalityEntity municipality;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id", referencedColumnName = "id", nullable = true)
	private WorkspaceEntity workspace;

	@OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
	private List<WorkspaceOperatorEntity> operators = new ArrayList<WorkspaceOperatorEntity>();

	@OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
	private List<WorkspaceManagerEntity> managers = new ArrayList<WorkspaceManagerEntity>();

	/*
	 * @Column(name = "municipality_area", precision = 10, scale = 2, nullable =
	 * true) private Double municipalityArea;
	 * 
	 * @Column(name = "number_alphanumeric_parcels", nullable = true) private Long
	 * numberAlphanumericParcels;
	 * 
	 * @Column(name = "version", nullable = false) private Long version;
	 * 
	 * @Column(name = "manager_code", nullable = false) private Long managerCode;
	 * 
	 * @Column(name = "observations", nullable = false, length = 255) private String
	 * observations;
	 * 
	 * @Column(name = "start_date", nullable = false)
	 * 
	 * @Temporal(TemporalType.DATE) private Date startDate;
	 * 
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "state_id", referencedColumnName = "id", nullable = false)
	 * private StateEntity state;
	 * 
	 * @OneToMany(mappedBy = "workspace") private List<SupportEntity> supports = new
	 * ArrayList<SupportEntity>();
	 * 
	 * @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL) private
	 * List<WorkspaceStateEntity> statesHistory = new
	 * ArrayList<WorkspaceStateEntity>();
	 */

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

	public List<WorkspaceManagerEntity> getManagers() {
		return managers;
	}

	public void setManagers(List<WorkspaceManagerEntity> managers) {
		this.managers = managers;
	}

}
