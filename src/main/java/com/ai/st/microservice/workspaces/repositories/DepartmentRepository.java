package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ai.st.microservice.workspaces.entities.DepartmentEntity;

public interface DepartmentRepository extends CrudRepository<DepartmentEntity, Long> {

	@Override
	List<DepartmentEntity> findAll();

	@Query("SELECT distinct d FROM DepartmentEntity d, MunicipalityEntity m, WorkspaceEntity w WHERE w.municipality.id = m.id AND m.department.id = d.id AND w.managerCode = :managerCode AND w.isActive = TRUE")
	List<DepartmentEntity> getDeparmentsByManagerCode(@Param("managerCode") Long managerCode);

}
