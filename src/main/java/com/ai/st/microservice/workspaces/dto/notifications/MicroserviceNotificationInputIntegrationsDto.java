package com.ai.st.microservice.workspaces.dto.notifications;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceNotificationInputIntegrationsDto implements Serializable {

	private static final long serialVersionUID = 3050930525504636650L;

	private Long userCode;
	private String email;
	private String type;
	private int status;
	private String integrationStatus;
	private String mpio;
	private String dpto;
	private Date integrationDate;

	public MicroserviceNotificationInputIntegrationsDto() {

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

	public String getIntegrationStatus() {
		return integrationStatus;
	}

	public void setIntegrationStatus(String integrationStatus) {
		this.integrationStatus = integrationStatus;
	}

	public Date getIntegrationDate() {
		return integrationDate;
	}

	public void setIntegrationDate(Date integrationDate) {
		this.integrationDate = integrationDate;
	}

}
