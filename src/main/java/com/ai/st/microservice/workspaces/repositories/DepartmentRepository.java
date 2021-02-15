package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ai.st.microservice.workspaces.entities.DepartmentEntity;

public interface DepartmentRepository extends CrudRepository<DepartmentEntity, Long> {

	@Override
	List<DepartmentEntity> findAll();

	@Query(nativeQuery = true, value = "select\n" + "	distinct d.*\n" + "from\n" + "	workspaces.departments d,\n"
			+ "	workspaces.municipalities m,\n" + "	workspaces.workspaces w,\n" + "	workspaces.workspace_managers wm\n"
			+ "where\n" + "	w.municipality_id = m.id\n" + "	and m.department_id = d.id\n"
			+ "	and wm.workspace_id = w.id \n" + "	and wm.manager_code = :managerCode \n" + "	and w.is_active = true")
	List<DepartmentEntity> getDeparmentsByManagerCode(@Param("managerCode") Long managerCode);
}
