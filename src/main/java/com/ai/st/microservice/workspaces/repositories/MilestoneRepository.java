package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.MilestoneEntity;

public interface MilestoneRepository extends CrudRepository<MilestoneEntity, Long> {

}
