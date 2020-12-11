package com.ai.st.microservice.workspaces.dto.geovisor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceSetupMapDto implements Serializable {

	private static final long serialVersionUID = 2755366603547812603L;

	private String name_conn;
	private String dbname;
	private String host;
	private int port;
	private String user;
	private String password;
	private String schema;
	private List<String> layers;

	public MicroserviceSetupMapDto() {
		this.layers = new ArrayList<String>();
	}

	public String getName_conn() {
		return name_conn;
	}

	public void setName_conn(String name_conn) {
		this.name_conn = name_conn;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public List<String> getLayers() {
		return layers;
	}

	public void setLayers(List<String> layers) {
		this.layers = layers;
	}

}
