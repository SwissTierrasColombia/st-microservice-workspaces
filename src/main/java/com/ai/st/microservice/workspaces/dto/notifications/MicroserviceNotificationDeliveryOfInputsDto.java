package com.ai.st.microservice.workspaces.dto.notifications;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceNotificationDeliveryOfInputsDto implements Serializable {

	private static final long serialVersionUID = 3050930525504636650L;

	private Long userCode;
	private String email;
	private String type;
	private int status;
	private String manager;
	private String mpio;
	private String dpto;
	private String supportFile;
	private Date requestDate;

	public MicroserviceNotificationDeliveryOfInputsDto() {

	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMpio() {
		return mpio;
	}

	public void setMpio(String mpio) {
		this.mpio = mpio;
	}

	public String getDpto() {
		return dpto;
	}

	public void setDpto(String dpto) {
		this.dpto = dpto;
	}

	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getSupportFile() {
		return supportFile;
	}

	public void setSupportFile(String supportFile) {
		this.supportFile = supportFile;
	}

}
