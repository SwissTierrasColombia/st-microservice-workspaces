package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceCreateSupplyRevisionDto implements Serializable {

	private static final long serialVersionUID = 5502646530977747659L;

	private String database;
	private String hostname;
	private String port;
	private String schema;
	private String username;
	private String password;
	private Long startBy;

	public MicroserviceCreateSupplyRevisionDto() {

	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
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

	public Long getStartBy() {
		return startBy;
	}

	public void setStartBy(Long startBy) {
		this.startBy = startBy;
	}

}
