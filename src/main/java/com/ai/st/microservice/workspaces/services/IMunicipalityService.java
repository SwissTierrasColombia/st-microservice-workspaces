package com.ai.st.microservice.workspaces.services;

import java.util.List;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;

public interface IMunicipalityService {

    Long getCount();

    MunicipalityEntity createMunicipality(MunicipalityEntity municipalityEntity);

    List<MunicipalityEntity> getMunicipalitiesByDepartmentId(Long departmentId);

    MunicipalityEntity getMunicipalityById(Long id);

    List<MunicipalityEntity> getMunicipalitiesByDepartmentIdAndManagerCode(Long departmentId, Long managerCode);

    MunicipalityEntity getMunicipalityByCode(String code);

    List<MunicipalityEntity> getMunicipalitiesByManagerCode(Long managerCode);

    List<MunicipalityEntity> getMunicipalitiesNotWorkspaceByDepartment(Long departmentId);

    List<MunicipalityEntity> getMunicipalitiesWhereManagerDoesNotBelongIn(Long managerCode, Long departmentId);

}
