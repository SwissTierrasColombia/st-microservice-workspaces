package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

public class CancelTaskDto implements Serializable {

    private static final long serialVersionUID = -3519926277652676501L;

    private String reason;

    public CancelTaskDto() {

    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
