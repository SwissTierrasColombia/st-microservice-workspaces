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
@Table(name = "integrations", schema = "workspaces")
public class IntegrationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id", referencedColumnName = "id", nullable = false)
	private WorkspaceEntity workspace;

	@Column(name = "started_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startedAt;

	@Column(name = "finished_at", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "integration_state_id", referencedColumnName = "id", nullable = false)
	private IntegrationStateEntity state;

	@Column(name = "hostname", nullable = false)
	private String hostname;

	@Column(name = "port", nullable = false)
	private String port;

	@Column(name = "database", nullable = false)
	private String database;

	@Column(name = "schema", nullable = false)
	private String schema;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "supply_cadastre_id", nullable = false)
	private Long supplyCadastreId;

	@Column(name = "supply_snr_id", nullable = false)
	private Long supplySnrId;

	@Column(name = "supply_ant_id", nullable = true)
	private Long supplyAntId;
	
	@Column(name = "url_map", nullable = true)
	private String urlMap;

	@OneToMany(mappedBy = "integration", cascade = CascadeType.ALL)
	private List<IntegrationStatEntity> stats = new ArrayList<IntegrationStatEntity>();

	@OneToMany(mappedBy = "integration", cascade = CascadeType.ALL)
	private List<IntegrationHistoryEntity> histories = new ArrayList<IntegrationHistoryEntity>();

	public IntegrationEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public IntegrationStateEntity getState() {
		return state;
	}

	public void setState(IntegrationStateEntity state) {
		this.state = state;
	}

	public WorkspaceEntity getWorkspace() {
		return workspace;
	}

	public void setWorkspace(WorkspaceEntity workspace) {
		this.workspace = workspace;
	}

	public List<IntegrationStatEntity> getStats() {
		return stats;
	}

	public void setStats(List<IntegrationStatEntity> stats) {
		this.stats = stats;
	}

	public Long getSupplyCadastreId() {
		return supplyCadastreId;
	}

	public void setSupplyCadastreId(Long supplyCadastreId) {
		this.supplyCadastreId = supplyCadastreId;
	}

	public Long getSupplySnrId() {
		return supplySnrId;
	}

	public void setSupplySnrId(Long supplySnrId) {
		this.supplySnrId = supplySnrId;
	}

	public Long getSupplyAntId() {
		return supplyAntId;
	}

	public void setSupplyAntId(Long supplyAntId) {
		this.supplyAntId = supplyAntId;
	}

	public List<IntegrationHistoryEntity> getHistories() {
		return histories;
	}

	public void setHistories(List<IntegrationHistoryEntity> histories) {
		this.histories = histories;
	}

	public String getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(String urlMap) {
		this.urlMap = urlMap;
	}

}
