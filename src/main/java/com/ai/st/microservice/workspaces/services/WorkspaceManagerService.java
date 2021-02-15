package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;
import com.ai.st.microservice.workspaces.repositories.WorkspaceManagerRepository;

@Service
public class WorkspaceManagerService implements IWorkspaceManagerService {

	@Autowired
	private WorkspaceManagerRepository workspaceManagerRepository;

	@Override
	@Transactional
	public WorkspaceManagerEntity createManager(WorkspaceManagerEntity manager) {
		return workspaceManagerRepository.save(manager);
	}

	@Override
	public WorkspaceManagerEntity getWorkspaceManagerById(Long workspaceManagerId) {
		return workspaceManagerRepository.findById(workspaceManagerId).orElse(null);
	}

	@Override
	@Transactional
	public WorkspaceManagerEntity updateWorkspaceManager(WorkspaceManagerEntity manager) {
		return workspaceManagerRepository.save(manager);
	}

	@Override
	@Transactional
	public void deleteWorkspaceManagerById(Long id) {
		workspaceManagerRepository.deleteById(id);
	}
	

}
