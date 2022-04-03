package com.ai.st.microservice.workspaces.services.tracing;

public enum TracingKeyword {
    USER_ID("userId"), USER_NAME("username"), USER_EMAIL("userEmail"), MANAGER_ID("managerId"),
    MANAGER_NAME("managerName"), AUTHORIZATION_HEADER("authorizationHeader"), DEPARTMENT_ID("departmentId"),
    IS_SUPER_ADMIN("isSuperAdmin"), IS_ADMIN("isAdmin"), IS_MANAGER("isManager"), IS_PROVIDER("isProvider"),
    IS_OPERATOR("isOperator"), BODY_REQUEST("bodyRequest"), PROVIDER_ID("providerId"), PROVIDER_NAME("providerName");

    private final String value;

    TracingKeyword(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
