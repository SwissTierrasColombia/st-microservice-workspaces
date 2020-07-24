package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceSupplyRevisionDto implements Serializable {

	private static final long serialVersionUID = 6236269390887662607L;

	private Long id;
	private String database;
	private String hostname;
	private String port;
	private String schema;
	private String username;
	private String password;
	private Long startBy;
	private Date startAt;
	private Date finishedAt;
	private Long finishedBy;
	private MicroserviceSupplyRequestedDto supplyRequested;

	public MicroserviceSupplyRevisionDto() {

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

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}

	public Long getFinishedBy() {
		return finishedBy;
	}

	public void setFinishedBy(Long finishedBy) {
		this.finishedBy = finishedBy;
	}

	public MicroserviceSupplyRequestedDto getSupplyRequested() {
		return supplyRequested;
	}

	public void setSupplyRequested(MicroserviceSupplyRequestedDto supplyRequested) {
		this.supplyRequested = supplyRequested;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
