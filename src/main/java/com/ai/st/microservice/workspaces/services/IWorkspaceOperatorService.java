package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;

public interface IWorkspaceOperatorService {

	public void deleteWorkspaceOperatorById(Long id);
	
	public WorkspaceOperatorEntity createOperator(WorkspaceOperatorEntity operator);
	
}
