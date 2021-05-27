package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.services.IDepartmentService;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DepartmentBusiness {

    private final IDepartmentService departmentService;

    public DepartmentBusiness(IDepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    public List<DepartmentDto> getDepartments() throws BusinessException {

        List<DepartmentDto> listDepartmentsDto = new ArrayList<>();

        List<DepartmentEntity> listDepartmentsEntity = departmentService.getAllDepartments();

        for (DepartmentEntity departmentEntity : listDepartmentsEntity) {

            DepartmentDto departmentDto = new DepartmentDto(departmentEntity.getId(), departmentEntity.getName(),
                    departmentEntity.getCode());

            listDepartmentsDto.add(departmentDto);

        }

        return listDepartmentsDto;
    }

    public List<DepartmentDto> getDepartmentsByManagerCode(Long managerCode) throws BusinessException {

        List<DepartmentDto> listDepartmentsDto = new ArrayList<>();

        List<DepartmentEntity> listDepartmentsEntity = departmentService.getDepartmentsByManagerCode(managerCode);

        for (DepartmentEntity departmentEntity : listDepartmentsEntity) {

            DepartmentDto departmentDto = new DepartmentDto(departmentEntity.getId(), departmentEntity.getName(),
                    departmentEntity.getCode());

            listDepartmentsDto.add(departmentDto);

        }

        return listDepartmentsDto;
    }

}
