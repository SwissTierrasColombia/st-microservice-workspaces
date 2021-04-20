package com.ai.st.microservice.workspaces.services;

import java.util.List;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;

public interface IWorkspaceService {

    WorkspaceEntity createWorkspace(WorkspaceEntity workspaceEntity);

    Long getCountByMunicipality(MunicipalityEntity municipalityEntity);

    List<WorkspaceEntity> getWorkspacesByMunicipality(MunicipalityEntity municipalityEntity);

    WorkspaceEntity getWorkspaceActiveByMunicipality(MunicipalityEntity municipalityEntity);

    WorkspaceEntity getWorkspaceById(Long id);

    WorkspaceEntity updateWorkspace(WorkspaceEntity workspaceEntity);

    List<WorkspaceEntity> getWorkspacesByManager(Long managerCode);

    List<WorkspaceEntity> getWorkspacesByDepartment(Long departmentId);

    void deleteWorkspaceById(Long workspaceId);

}
