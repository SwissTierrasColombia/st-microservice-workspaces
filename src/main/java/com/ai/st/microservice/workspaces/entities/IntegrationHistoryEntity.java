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
@Table(name = "integrations_histories", schema = "workspaces")
public class IntegrationHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "integration_id", referencedColumnName = "id", nullable = false)
	private IntegrationEntity integration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "integration_state_id", referencedColumnName = "id", nullable = false)
	private IntegrationStateEntity state;

	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	public IntegrationHistoryEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IntegrationEntity getIntegration() {
		return integration;
	}

	public void setIntegration(IntegrationEntity integration) {
		this.integration = integration;
	}

	public IntegrationStateEntity getState() {
		return state;
	}

	public void setState(IntegrationStateEntity state) {
		this.state = state;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
