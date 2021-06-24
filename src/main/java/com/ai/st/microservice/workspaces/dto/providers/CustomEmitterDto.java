package com.ai.st.microservice.workspaces.dto.providers;

import com.ai.st.microservice.common.dto.providers.MicroserviceEmitterDto;

import java.io.Serializable;

public class CustomEmitterDto extends MicroserviceEmitterDto implements Serializable {

    private Object user;

    public CustomEmitterDto() {
        super();
    }

    public CustomEmitterDto(MicroserviceEmitterDto response) {
        this.setId(response.getId());
        this.setCreatedAt(response.getCreatedAt());
        this.setEmitterCode(response.getEmitterCode());
        this.setEmitterType(response.getEmitterType());
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

}
