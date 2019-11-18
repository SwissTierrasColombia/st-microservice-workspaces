package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;

public interface WorkspaceRepository extends CrudRepository<WorkspaceEntity, Long> {

}
