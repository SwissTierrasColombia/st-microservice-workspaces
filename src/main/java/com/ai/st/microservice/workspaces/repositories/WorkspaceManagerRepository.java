package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;

import java.util.List;

public interface WorkspaceManagerRepository extends CrudRepository<WorkspaceManagerEntity, Long> {

    List<WorkspaceManagerEntity> findByManagerCode(Long managerCode);

}
