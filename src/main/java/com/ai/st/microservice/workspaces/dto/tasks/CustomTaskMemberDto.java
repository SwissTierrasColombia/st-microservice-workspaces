package com.ai.st.microservice.workspaces.dto.tasks;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.tasks.MicroserviceTaskMemberDto;

import java.io.Serializable;

public class CustomTaskMemberDto extends MicroserviceTaskMemberDto implements Serializable {

    private MicroserviceUserDto user;

    public CustomTaskMemberDto() {
        super();
    }

    public CustomTaskMemberDto(MicroserviceTaskMemberDto response) {
        super();
        this.setId(response.getId());
        this.setMemberCode(response.getMemberCode());
        this.setCreatedAt(response.getCreatedAt());
    }

    public MicroserviceUserDto getUser() {
        return user;
    }

    public void setUser(MicroserviceUserDto user) {
        this.user = user;
    }

}
