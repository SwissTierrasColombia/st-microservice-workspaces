package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IDepartmentService;

@Component
public class DepartmentBusiness {

    @Autowired
    private IDepartmentService departmentService;

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
