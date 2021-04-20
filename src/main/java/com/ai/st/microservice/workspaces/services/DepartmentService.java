package com.ai.st.microservice.workspaces.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.repositories.DepartmentRepository;

@Service
public class DepartmentService implements IDepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Long getCount() {
        return departmentRepository.count();
    }

    @Override
    @Transactional
    public DepartmentEntity createDepartment(DepartmentEntity departmentEntity) {
        return departmentRepository.save(departmentEntity);
    }

    @Override
    public List<DepartmentEntity> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public List<DepartmentEntity> getDepartmentsByManagerCode(Long managerCode) {
        return departmentRepository.getDeparmentsByManagerCode(managerCode);
    }

}
