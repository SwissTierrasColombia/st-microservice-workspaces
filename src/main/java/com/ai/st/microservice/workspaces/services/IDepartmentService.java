package com.ai.st.microservice.workspaces.services;

import java.util.List;

import com.ai.st.microservice.workspaces.entities.DepartmentEntity;

public interface IDepartmentService {

    Long getCount();

    DepartmentEntity createDepartment(DepartmentEntity departmentEntity);

    List<DepartmentEntity> getAllDepartments();

    List<DepartmentEntity> getDepartmentsByManagerCode(Long managerCode);

}
