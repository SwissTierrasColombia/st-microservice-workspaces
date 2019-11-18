package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;

public interface MunicipalityRepository extends CrudRepository<MunicipalityEntity, Long> {

	@Query("SELECT m FROM MunicipalityEntity m WHERE m.department.id = :departmentId")
	List<MunicipalityEntity> getMunicipalitiesByDepartmentId(@Param("departmentId") Long departmentId);

}
