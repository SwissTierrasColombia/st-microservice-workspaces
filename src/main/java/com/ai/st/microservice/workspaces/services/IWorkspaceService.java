package com.ai.st.microservice.workspaces.services;

import java.util.List;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;

public interface IWorkspaceService {

	public WorkspaceEntity createWorkspace(WorkspaceEntity workspaceEntity);

	public Long getCountByMunicipality(MunicipalityEntity municipalityEntity);

	public List<WorkspaceEntity> getWorkspacesByMunicipality(MunicipalityEntity municipalityEntity);

	public WorkspaceEntity getWorkspaceActiveByMunicipality(MunicipalityEntity municipalityEntity);

	public WorkspaceEntity getWorkspaceById(Long id);

	public WorkspaceEntity updateWorkspace(WorkspaceEntity workspaceEntity);

	public List<WorkspaceEntity> getWorkspacesByManagerAndIsActive(Long managerCode, Boolean isActive);
	
	public List<WorkspaceEntity> getWorkspacesByDepartment(Long departmentId);
	
	public void deleteWorkspaceById(Long workspaceId);

}
