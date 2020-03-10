package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;

public interface IntegrationStateRepository extends CrudRepository<IntegrationStateEntity, Long> {

}
