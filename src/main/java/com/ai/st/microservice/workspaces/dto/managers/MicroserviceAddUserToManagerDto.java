package com.ai.st.microservice.workspaces.dto.managers;

import java.io.Serializable;

public class MicroserviceAddUserToManagerDto implements Serializable {

	private static final long serialVersionUID = -7410047439578763438L;

	private Long userCode;
	private Long managerId;
	private Long profileId;

	public MicroserviceAddUserToManagerDto() {

	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

	public Long getProfileId() {
		return profileId;
	}

	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}

}
