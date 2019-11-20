package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;

public interface MunicipalityRepository extends CrudRepository<MunicipalityEntity, Long> {

	@Query("SELECT m FROM MunicipalityEntity m WHERE m.department.id = :departmentId")
	List<MunicipalityEntity> getMunicipalitiesByDepartmentId(@Param("departmentId") Long departmentId);

	@Query("SELECT m FROM MunicipalityEntity m, WorkspaceEntity w  WHERE m.department.id = :departmentId AND w.municipality.id = m.id AND w.managerCode = :managerCode AND w.isActive = TRUE")
	List<MunicipalityEntity> getMunicipalitiesByDepartmentIdAndManagerCode(@Param("departmentId") Long departmentId,
			@Param("managerCode") Long managerCode);

}
