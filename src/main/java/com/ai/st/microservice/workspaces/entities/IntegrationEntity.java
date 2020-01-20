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
@Table(name = "integrations", schema = "workspaces")
public class IntegrationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipality_id", referencedColumnName = "id", nullable = false)
	private MunicipalityEntity municipality;

	@Column(name = "started_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startedAt;

	@Column(name = "finished_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishedAt;

	@Column(name = "pending", nullable = false)
	private Boolean pending;

	public IntegrationEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MunicipalityEntity getMunicipality() {
		return municipality;
	}

	public void setMunicipality(MunicipalityEntity municipality) {
		this.municipality = municipality;
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

	public Boolean getPending() {
		return pending;
	}

	public void setPending(Boolean pending) {
		this.pending = pending;
	}

}
