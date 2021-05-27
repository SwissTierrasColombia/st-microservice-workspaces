package com.ai.st.microservice.workspaces.dto.tasks;

import java.io.Serializable;
import java.util.Date;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;

public class MicroserviceTaskMemberDto implements Serializable {

    private static final long serialVersionUID = 7581006574042957605L;

    private Long id;
    private Long memberCode;
    private Date createdAt;

    private MicroserviceUserDto user;

    public MicroserviceTaskMemberDto() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(Long memberCode) {
        this.memberCode = memberCode;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public MicroserviceUserDto getUser() {
        return user;
    }

    public void setUser(MicroserviceUserDto user) {
        this.user = user;
    }

}
