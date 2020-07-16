package com.ai.st.microservice.workspaces.dto.ili;

import java.io.Serializable;

public class MicroserviceExecuteQueryUpdateToRevisionDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String databaseHost;
	private String databasePort;
	private String databaseSchema;
	private String databaseUsername;
	private String databasePassword;
	private String databaseName;
	private String versionModel;
	private Long conceptId;
	private Long boundarySpaceId;
	private String urlFile;
	private Long entityId;
	private String namespace;

	public MicroserviceExecuteQueryUpdateToRevisionDto() {

	}

	public String getDatabaseHost() {
		return databaseHost;
	}

	public void setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
	}

	public String getDatabasePort() {
		return databasePort;
	}

	public void setDatabasePort(String databasePort) {
		this.databasePort = databasePort;
	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}

	public void setDatabaseSchema(String databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getVersionModel() {
		return versionModel;
	}

	public void setVersionModel(String versionModel) {
		this.versionModel = versionModel;
	}

	public Long getConceptId() {
		return conceptId;
	}

	public void setConceptId(Long conceptId) {
		this.conceptId = conceptId;
	}

	public Long getBoundarySpaceId() {
		return boundarySpaceId;
	}

	public void setBoundarySpaceId(Long boundarySpaceId) {
		this.boundarySpaceId = boundarySpaceId;
	}

	public String getUrlFile() {
		return urlFile;
	}

	public void setUrlFile(String urlFile) {
		this.urlFile = urlFile;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
