package com.ai.st.microservice.workspaces.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.repositories.MunicipalityRepository;

@Service
public class MunicipalityService implements IMunicipalityService {

    private final MunicipalityRepository municipalityRepository;

    public MunicipalityService(MunicipalityRepository municipalityRepository) {
        this.municipalityRepository = municipalityRepository;
    }

    @Override
    public Long getCount() {
        return municipalityRepository.count();
    }

    @Override
    @Transactional
    public MunicipalityEntity createMunicipality(MunicipalityEntity municipalityEntity) {
        return municipalityRepository.save(municipalityEntity);
    }

    @Override
    public List<MunicipalityEntity> getMunicipalitiesByDepartmentId(Long departmentId) {
        return municipalityRepository.getMunicipalitiesByDepartmentId(departmentId);
    }

    @Override
    public MunicipalityEntity getMunicipalityById(Long id) {
        return municipalityRepository.findById(id).orElse(null);
    }

    @Override
    public List<MunicipalityEntity> getMunicipalitiesByDepartmentIdAndManagerCode(Long departmentId, Long managerCode) {
        return municipalityRepository.getMunicipalitiesByDepartmentIdAndManagerCode(departmentId, managerCode);
    }

    @Override
    public MunicipalityEntity getMunicipalityByCode(String code) {
        return municipalityRepository.findByCode(code);
    }

    @Override
    public List<MunicipalityEntity> getMunicipalitiesByManagerCode(Long managerCode) {
        return municipalityRepository.getMunicipalitiesByManagerCode(managerCode);
    }

    @Override
    public List<MunicipalityEntity> getMunicipalitiesNotWorkspaceByDepartment(Long departmentId) {
        return municipalityRepository.getMunicipalitiesNotWorkspaceInDepartment(departmentId);
    }

    @Override
    public List<MunicipalityEntity> getMunicipalitiesWhereManagerDoesNotBelongIn(Long managerCode, Long departmentId) {
        return municipalityRepository.getMunicipalitiesWhereManagerDoesNotBelongIn(managerCode, departmentId);
    }

}
