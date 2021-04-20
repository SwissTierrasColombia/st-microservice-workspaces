package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;

public interface IWorkspaceManagerService {

    WorkspaceManagerEntity createManager(WorkspaceManagerEntity manager);

    WorkspaceManagerEntity getWorkspaceManagerById(Long workspaceManagerId);

    WorkspaceManagerEntity updateWorkspaceManager(WorkspaceManagerEntity manager);

    void deleteWorkspaceManagerById(Long id);

}
