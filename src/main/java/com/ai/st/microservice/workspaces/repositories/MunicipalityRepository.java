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

	MunicipalityEntity findByCode(String code);

	@Query("SELECT m FROM MunicipalityEntity m, WorkspaceEntity w  WHERE m.id = w.municipality.id AND w.managerCode = :managerCode")
	List<MunicipalityEntity> getMunicipalitiesByManagerCode(@Param("managerCode") Long managerCode);

	@Query(nativeQuery = true, value = "select m.* from workspaces.municipalities m where m.id  not in (select "
			+ "m2.id\n" + "from\n" + "workspaces.municipalities m2 ,\n" + "workspaces.workspaces w \n"
			+ "where w.municipality_id = m2.id\n" + "and w.manager_code = :managerCode)")
	List<MunicipalityEntity> getMunicipalitiesNotIntManagerCode(@Param("managerCode") Long managerCode);

}
