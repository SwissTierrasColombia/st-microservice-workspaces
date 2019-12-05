package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.StateEntity;

public interface StateRepository extends CrudRepository<StateEntity, Long> {

}
