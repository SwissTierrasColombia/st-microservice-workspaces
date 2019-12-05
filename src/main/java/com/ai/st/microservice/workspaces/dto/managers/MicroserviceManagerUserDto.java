package com.ai.st.microservice.workspaces.dto.managers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceManagerUserDto implements Serializable {

	private static final long serialVersionUID = 130761780294600808L;

	private Long userCode;
	private List<MicroserviceManagerProfileDto> profiles;

	public MicroserviceManagerUserDto() {
		this.profiles = new ArrayList<MicroserviceManagerProfileDto>();
	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public List<MicroserviceManagerProfileDto> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<MicroserviceManagerProfileDto> profiles) {
		this.profiles = profiles;
	}

}
