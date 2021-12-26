package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;

import java.util.List;

public interface WorkspaceOperatorRepository extends CrudRepository<WorkspaceOperatorEntity, Long> {

    List<WorkspaceOperatorEntity> findByOperatorCode(Long operatorCode);

    List<WorkspaceOperatorEntity> findByManagerCode(Long managerCode);

}
