package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;

public interface WorkspaceOperatorRepository extends CrudRepository<WorkspaceOperatorEntity, Long> {

}
