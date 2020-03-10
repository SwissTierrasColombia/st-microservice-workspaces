package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.IntegrationStatEntity;

public interface IntegrationStatRepository extends CrudRepository<IntegrationStatEntity, Long> {

}
