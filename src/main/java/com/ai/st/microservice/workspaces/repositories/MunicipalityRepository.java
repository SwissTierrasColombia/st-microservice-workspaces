package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;

public interface MunicipalityRepository extends CrudRepository<MunicipalityEntity, Long> {

	@Query("SELECT m FROM MunicipalityEntity m WHERE m.department.id = :departmentId")
	List<MunicipalityEntity> getMunicipalitiesByDepartmentId(@Param("departmentId") Long departmentId);

	@Query(nativeQuery = true, value = "select\n" + "	m.*\n" + "from\n" + "	workspaces.municipalities m,\n"
			+ "	workspaces.workspaces w,\n" + "	workspaces.workspace_managers wm \n" + "where\n"
			+ "	w.municipality_id = m.id\n" + "	and wm.workspace_id = w.id \n"
			+ "	and wm.manager_code = :managerCode \n" + "	and m.department_id = :departmentId \n"
			+ "	and w.is_active = true")
	List<MunicipalityEntity> getMunicipalitiesByDepartmentIdAndManagerCode(@Param("departmentId") Long departmentId,
			@Param("managerCode") Long managerCode);

	MunicipalityEntity findByCode(String code);

	@Query(nativeQuery = true, value = "select\n" + "	m.* \n" + "from\n" + "	workspaces.municipalities m,\n"
			+ "	workspaces.workspaces w,\n" + "	workspaces.workspace_managers wm \n" + "where\n"
			+ "	m.id = w.municipality_id\n" + "	and w.id = wm.workspace_id\n" + "	and wm.manager_code = :managerCode")
	List<MunicipalityEntity> getMunicipalitiesByManagerCode(@Param("managerCode") Long managerCode);

	@Query(nativeQuery = true, value = "select\n" + "	m.*\n" + "from\n"
			+ "	workspaces.municipalities m, workspaces.departments d\n" + "where\n" + "	d.id  = m.department_id \n"
			+ "	and d.id = :departmentId \n" + "	and m.id not in (\n" + "	select\n" + "		m2.id\n"
			+ "	from\n" + "		workspaces.municipalities m2 , workspaces.workspaces w\n" + "	where\n"
			+ "		w.municipality_id = m2.id)")
	List<MunicipalityEntity> getMunicipalitiesNotWorkspaceInDepartment(@Param("departmentId") Long departmentId);

	@Query(nativeQuery = true, value = "select\n" + "	m.*\n" + "from\n" + "	workspaces.municipalities m,\n"
			+ "	workspaces.departments d\n" + "where\n" + "	d.id = m.department_id\n" + "	and d.id = :departmentId \n"
			+ "	and m.id not in (\n" + "	select\n" + "		m2.id\n" + "	from\n"
			+ "		workspaces.municipalities m2 ,\n" + "		workspaces.workspaces w,\n"
			+ "		workspaces.workspace_managers wm\n" + "	where\n"
			+ "		w.municipality_id = m2.id and wm.manager_code = :managerCode and wm.workspace_id  = w.id)")
	List<MunicipalityEntity> getMunicipalitiesWhereManagerDoesNotBelongIn(@Param("managerCode") Long managerCode,
			@Param("departmentId") Long departmentId);

}
