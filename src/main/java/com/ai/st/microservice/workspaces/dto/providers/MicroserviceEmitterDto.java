package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceEmitterDto implements Serializable {

    private static final long serialVersionUID = 1251788414545388461L;

	private Long id;
	private Date createdAt;
	private Long emitterCode;
	private String emitterType;

	private Object user;

    public MicroserviceEmitterDto() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long getEmitterCode() {
        return emitterCode;
    }

    public void setEmitterCode(Long emitterCode) {
        this.emitterCode = emitterCode;
    }

    public String getEmitterType() {
        return emitterType;
    }

    public void setEmitterType(String emitterType) {
        this.emitterType = emitterType;
    }

	public Object getUser() {
		return user;
	}

	public void setUser(Object user) {
		this.user = user;
	}

}
