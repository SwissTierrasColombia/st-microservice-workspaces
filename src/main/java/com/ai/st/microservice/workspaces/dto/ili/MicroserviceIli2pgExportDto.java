package com.ai.st.microservice.workspaces.dto.ili;

import java.io.Serializable;

public class MicroserviceIli2pgExportDto implements Serializable {

	private static final long serialVersionUID = -4791993304492124731L;

	private String databaseHost;
	private String databasePort;
	private String databaseSchema;
	private String databaseUsername;
	private String databasePassword;
	private String databaseName;
	private String pathFileXTF;
	private Long integrationId;
	private Boolean withStats;

	public MicroserviceIli2pgExportDto() {

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

	public String getPathFileXTF() {
		return pathFileXTF;
	}

	public void setPathFileXTF(String pathFileXTF) {
		this.pathFileXTF = pathFileXTF;
	}

	public Long getIntegrationId() {
		return integrationId;
	}

	public void setIntegrationId(Long integrationId) {
		this.integrationId = integrationId;
	}

	public Boolean getWithStats() {
		return withStats;
	}

	public void setWithStats(Boolean withStats) {
		this.withStats = withStats;
	}

}
