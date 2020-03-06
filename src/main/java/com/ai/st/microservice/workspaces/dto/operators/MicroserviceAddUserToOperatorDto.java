package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;

public class MicroserviceAddUserToOperatorDto implements Serializable {

	private static final long serialVersionUID = 3281389121792970476L;

	private Long userCode;
	private Long operatorId;

	public MicroserviceAddUserToOperatorDto() {

	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public Long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Long operatorId) {
		this.operatorId = operatorId;
	}

}
