package com.ai.st.microservice.workspaces.dto.rabbitmq;

import java.io.Serializable;

public class UploadFileMessageDto implements Serializable {

	private static final long serialVersionUID = 3050930525504636650L;

	private String filename;
	private String namespace;
	private String driver;
	private byte[] file;
	private boolean zip;

	public UploadFileMessageDto(String filename, String namespace, String driver, byte[] file, boolean zip) {
		this.filename = filename;
		this.namespace = namespace;
		this.driver = driver;
		this.file = file;
		this.zip = zip;
	}

	public String getFilename() {
		return filename;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getDriver() {
		return driver;
	}

	public byte[] getFile() {
		return file;
	}

	public boolean isZip() {
		return zip;
	}

}
