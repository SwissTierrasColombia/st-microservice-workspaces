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

    @Query(nativeQuery = true, value = "select \n" + "w.*\n" + "from \n" + "workspaces.workspaces w ,\n"
            + "workspaces.workspace_managers wm\n" + "where\n" + "w.is_active = true\n"
            + "and wm.workspace_id  = w.id \n" + "and wm.manager_code = :managerCode")
    List<WorkspaceEntity> findByManagerCode(@Param("managerCode") Long departmentId);

    @Query(nativeQuery = true, value = "select\n" + "	w.*\n" + "from\n" + "	workspaces.workspaces w,\n"
            + "	workspaces.municipalities m,\n" + "	workspaces.departments d\n" + "where\n"
            + "	w.municipality_id = m.id\n" + "	and d.id = m.department_id\n" + "	and d.id = :departmentId")
    List<WorkspaceEntity> getWorkspacesByDepartment(@Param("departmentId") Long departmentId);

}
