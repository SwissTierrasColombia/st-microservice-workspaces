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
@Table(name = "integrations_stats", schema = "workspaces")
public class IntegrationStatEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "integration_id", referencedColumnName = "id", nullable = false)
	private IntegrationEntity integration;

	@Column(name = "snr_records_number", nullable = false)
	private Long snrRecordsNumber;

	@Column(name = "cadastre_records_number", nullable = false)
	private Long cadastreRecordsNumber;

	@Column(name = "ant_records_number", nullable = true)
	private Long antRecordsNumber;

	@Column(name = "match_number", nullable = false)
	private Long matchNumber;

	@Column(name = "percentage", precision = 10, scale = 2, nullable = false)
	private Double percentage;

	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	public IntegrationStatEntity() {

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

	public Long getSnrRecordsNumber() {
		return snrRecordsNumber;
	}

	public void setSnrRecordsNumber(Long snrRecordsNumber) {
		this.snrRecordsNumber = snrRecordsNumber;
	}

	public Long getCadastreRecordsNumber() {
		return cadastreRecordsNumber;
	}

	public void setCadastreRecordsNumber(Long cadastreRecordsNumber) {
		this.cadastreRecordsNumber = cadastreRecordsNumber;
	}

	public Long getAntRecordsNumber() {
		return antRecordsNumber;
	}

	public void setAntRecordsNumber(Long antRecordsNumber) {
		this.antRecordsNumber = antRecordsNumber;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Long getMatchNumber() {
		return matchNumber;
	}

	public void setMatchNumber(Long matchNumber) {
		this.matchNumber = matchNumber;
	}

}
