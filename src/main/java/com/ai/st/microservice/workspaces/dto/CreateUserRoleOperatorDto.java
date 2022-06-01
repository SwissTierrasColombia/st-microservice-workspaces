package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateUserRoleOperatorDto", description = "Create User Role Operator Dto")
public class CreateUserRoleOperatorDto implements Serializable {

    private static final long serialVersionUID = -8970830341208681180L;

    @ApiModelProperty(required = true, notes = "Role ID")
    private Long roleId;

    @ApiModelProperty(required = true, notes = "Operator ID")
    private Long operatorId;

    public CreateUserRoleOperatorDto() {

    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    @Override
    public String toString() {
        return "CreateUserRoleOperatorDto{" + "roleId=" + roleId + ", operatorId=" + operatorId + '}';
    }
}
