package com.ai.st.microservice.workspaces.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.repositories.WorkspaceRepository;

@Service
public class WorkspaceService implements IWorkspaceService {

	@Autowired
	private WorkspaceRepository workspaceRepository;

	@Override
	@Transactional
	public WorkspaceEntity createWorkspace(WorkspaceEntity workspaceEntity) {
		return workspaceRepository.save(workspaceEntity);
	}

	@Override
	public Long getCountByMunicipality(MunicipalityEntity municipalityEntity) {
		return workspaceRepository.countByMunicipality(municipalityEntity);
	}

	@Override
	public List<WorkspaceEntity> getWorkspacesByMunicipality(MunicipalityEntity municipalityEntity) {
		return workspaceRepository.findByMunicipality(municipalityEntity);
	}

	@Override
	public WorkspaceEntity getWorkspaceActiveByMunicipality(MunicipalityEntity municipalityEntity) {
		return workspaceRepository.findByIsActiveAndMunicipality(true, municipalityEntity);
	}

	@Override
	public WorkspaceEntity getWorkspaceById(Long id) {
		return workspaceRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public WorkspaceEntity updateWorkspace(WorkspaceEntity workspaceEntity) {
		return workspaceRepository.save(workspaceEntity);
	}

	@Override
	public List<WorkspaceEntity> getWorkspacesByManagerAndIsActive(Long managerCode, Boolean isActive) {
		/**
		 * Refactoring ...
		 * 
		 * Before:
		 * 
		 * return workspaceRepository.findByManagerCodeAndIsActive(managerCode,
		 * isActive);
		 */
		return null;
	}

	@Override
	public List<WorkspaceEntity> getWorkspacesByDepartment(Long departmentId) {
		return workspaceRepository.getWorkspacesByDepartment(departmentId);
	}

	@Override
	public void deleteWorkspaceById(Long workspaceId) {
		workspaceRepository.deleteById(workspaceId);
	}

}
