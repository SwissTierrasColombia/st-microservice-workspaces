package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.DepartmentEntity;

public interface DepartmentRepository extends CrudRepository<DepartmentEntity, Long> {

	@Override
	List<DepartmentEntity> findAll();

}
