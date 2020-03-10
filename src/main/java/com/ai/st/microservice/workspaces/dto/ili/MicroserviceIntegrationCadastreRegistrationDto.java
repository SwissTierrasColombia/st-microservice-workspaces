package com.ai.st.microservice.workspaces.dto.ili;

import java.io.Serializable;

public class MicroserviceIntegrationCadastreRegistrationDto implements Serializable {

	private static final long serialVersionUID = -2379043053790590513L;

	private String databaseHost;
	private String databasePort;
	private String databaseSchema;
	private String databaseUsername;
	private String databasePassword;
	private String databaseName;
	private String cadastrePathXTF;
	private String registrationPathXTF;
	private Long integrationId;
	private String versionModel;

	public MicroserviceIntegrationCadastreRegistrationDto() {

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

	public String getCadastrePathXTF() {
		return cadastrePathXTF;
	}

	public void setCadastrePathXTF(String cadastrePathXTF) {
		this.cadastrePathXTF = cadastrePathXTF;
	}

	public String getRegistrationPathXTF() {
		return registrationPathXTF;
	}

	public void setRegistrationPathXTF(String registrationPathXTF) {
		this.registrationPathXTF = registrationPathXTF;
	}

	public Long getIntegrationId() {
		return integrationId;
	}

	public void setIntegrationId(Long integrationId) {
		this.integrationId = integrationId;
	}

	public String getVersionModel() {
		return versionModel;
	}

	public void setVersionModel(String versionModel) {
		this.versionModel = versionModel;
	}

}
