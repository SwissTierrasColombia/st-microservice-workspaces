package com.ai.st.microservice.workspaces.dto.notifications;

import java.io.Serializable;

public class MicroserviceNotificationNewUserDto implements Serializable {

	private static final long serialVersionUID = 3050930525504636650L;

	private Long userCode;
	private String email;
	private String type;
	private int status;
	private String user;
	private String password;
	private String profile;

	public MicroserviceNotificationNewUserDto() {

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

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

}
