package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;

public interface IWorkspaceManagerService {

	public WorkspaceManagerEntity createManager(WorkspaceManagerEntity manager);

	public WorkspaceManagerEntity getWorkspaceManagerById(Long workspaceManagerId);

	public WorkspaceManagerEntity updateWorkspaceManager(WorkspaceManagerEntity manager);

	public void deleteWorkspaceManagerById(Long id);

}
