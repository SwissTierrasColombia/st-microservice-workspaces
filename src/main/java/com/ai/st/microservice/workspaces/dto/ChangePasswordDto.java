package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ChangePasswordDto", description = "Change Password Dto")
public class ChangePasswordDto implements Serializable {

    private static final long serialVersionUID = -2819572836734055387L;

    @ApiModelProperty(required = true, notes = "Password")
    private String password;

    public ChangePasswordDto() {

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
