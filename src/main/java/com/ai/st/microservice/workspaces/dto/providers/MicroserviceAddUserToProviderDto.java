package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceAddUserToProviderDto implements Serializable {

	private static final long serialVersionUID = 1453826822762079124L;

	private Long userCode;
	private Long providerId;
	private Long profileId;

	public MicroserviceAddUserToProviderDto() {

	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public Long getProviderId() {
		return providerId;
	}

	public void setProviderId(Long providerId) {
		this.providerId = providerId;
	}

	public Long getProfileId() {
		return profileId;
	}

	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}

}
