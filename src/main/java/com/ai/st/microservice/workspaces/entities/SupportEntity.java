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
@Table(name = "supports", schema = "workspaces")
public class SupportEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "observations", nullable = true, length = 255)
	private String observations;

	@Column(name = "url_documentary_repository", nullable = true, length = 255)
	private String urlDocumentaryRepository;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id", referencedColumnName = "id", nullable = false)
	private WorkspaceEntity workspace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "milestone_id", referencedColumnName = "id", nullable = false)
	private MilestoneEntity milestone;

	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	public SupportEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public String getUrlDocumentaryRepository() {
		return urlDocumentaryRepository;
	}

	public void setUrlDocumentaryRepository(String urlDocumentaryRepository) {
		this.urlDocumentaryRepository = urlDocumentaryRepository;
	}

	public WorkspaceEntity getWorkspace() {
		return workspace;
	}

	public void setWorkspace(WorkspaceEntity workspace) {
		this.workspace = workspace;
	}

	public MilestoneEntity getMilestone() {
		return milestone;
	}

	public void setMilestone(MilestoneEntity milestone) {
		this.milestone = milestone;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
