package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;

public interface IWorkspaceOperatorService {

    void deleteWorkspaceOperatorById(Long id);

    WorkspaceOperatorEntity createOperator(WorkspaceOperatorEntity operator);

    WorkspaceOperatorEntity getWorkspaceOperatorById(Long id);

    WorkspaceOperatorEntity updateOperator(WorkspaceOperatorEntity operator);

}
