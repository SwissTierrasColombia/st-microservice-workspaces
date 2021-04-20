package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;

public interface WorkspaceManagerRepository extends CrudRepository<WorkspaceManagerEntity, Long> {

}
