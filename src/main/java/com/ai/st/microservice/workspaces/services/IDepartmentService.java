package com.ai.st.microservice.workspaces.services;

import java.util.List;

import com.ai.st.microservice.workspaces.entities.DepartmentEntity;

public interface IDepartmentService {

	public Long getCount();

	public DepartmentEntity createDepartment(DepartmentEntity departmentEntity);

	public List<DepartmentEntity> getAllDepartments();

	public List<DepartmentEntity> getDepartmentsByManagerCode(Long managerCode);

}
