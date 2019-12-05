package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.List;

public class MicroserviceProviderUserDto implements Serializable {

	private static final long serialVersionUID = -5467504093623630678L;

	private Long userCode;
	private List<MicroserviceProviderProfileDto> profiles;

	public MicroserviceProviderUserDto() {

	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public List<MicroserviceProviderProfileDto> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<MicroserviceProviderProfileDto> profiles) {
		this.profiles = profiles;
	}

}
