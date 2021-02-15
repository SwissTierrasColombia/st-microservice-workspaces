package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;

public interface WorkspaceRepository extends CrudRepository<WorkspaceEntity, Long> {

	Long countByMunicipality(MunicipalityEntity municipalityEntity);

	List<WorkspaceEntity> findByMunicipality(MunicipalityEntity municipality);

	WorkspaceEntity findByIsActiveAndMunicipality(Boolean isActive, MunicipalityEntity municipality);

	/**
	 * TODO: Refactoring ...
	 * 
	 * disabled temporally
	 * 
	 */
	//List<WorkspaceEntity> findByManagerCodeAndIsActive(Long managerCode, Boolean isActive);

	@Query(nativeQuery = true, value = "select\n" + "	w.*\n" + "from\n" + "	workspaces.workspaces w,\n"
			+ "	workspaces.municipalities m,\n" + "	workspaces.departments d\n" + "where\n"
			+ "	w.municipality_id = m.id\n" + "	and d.id = m.department_id\n" + "	and d.id = :departmentId")
	List<WorkspaceEntity> getWorkspacesByDepartment(@Param("departmentId") Long departmentId);

}
