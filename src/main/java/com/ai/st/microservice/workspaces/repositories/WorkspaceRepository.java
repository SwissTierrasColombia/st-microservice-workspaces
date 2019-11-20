package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;

public interface WorkspaceRepository extends CrudRepository<WorkspaceEntity, Long> {

	Long countByMunicipality(MunicipalityEntity municipalityEntity);

	List<WorkspaceEntity> findByMunicipality(MunicipalityEntity municipality);

	WorkspaceEntity findByIsActiveAndMunicipality(Boolean isActive, MunicipalityEntity municipality);

}
