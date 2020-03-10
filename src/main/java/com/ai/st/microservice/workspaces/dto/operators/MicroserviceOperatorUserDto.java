package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;

public class MicroserviceOperatorUserDto implements Serializable {

	private static final long serialVersionUID = 8040320082459436795L;

	private Long userCode;

	public MicroserviceOperatorUserDto() {

	}

	public MicroserviceOperatorUserDto(Long userCode) {
		super();
		this.userCode = userCode;
	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

}
