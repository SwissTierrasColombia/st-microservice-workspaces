package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.ManagerFeignClient;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.common.business.RoleBusiness;
import com.ai.st.microservice.common.dto.operators.MicroserviceOperatorDto;

import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.services.IWorkspaceOperatorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import feign.FeignException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ManagerMicroserviceBusiness {

    private final Logger log = LoggerFactory.getLogger(ManagerMicroserviceBusiness.class);

    private final ManagerFeignClient managerClient;
    private final IWorkspaceOperatorService workspaceOperatorService;
    private final OperatorMicroserviceBusiness operatorBusiness;

    public ManagerMicroserviceBusiness(ManagerFeignClient managerClient, IWorkspaceOperatorService workspaceOperatorService,
                                       OperatorMicroserviceBusiness operatorBusiness) {
        this.managerClient = managerClient;
        this.workspaceOperatorService = workspaceOperatorService;
        this.operatorBusiness = operatorBusiness;
    }

    public MicroserviceManagerDto getManagerById(Long managerId) {

        MicroserviceManagerDto managerDto = null;

        try {

            managerDto = managerClient.findById(managerId);

        } catch (Exception e) {
            log.error("No se ha podido consultar el gestor: " + e.getMessage());
        }

        return managerDto;
    }

    public MicroserviceManagerDto getManagerByUserCode(Long userCode) {

        MicroserviceManagerDto managerDto = null;

        try {
            managerDto = managerClient.findByUserCode(userCode);
        } catch (Exception e) {
            log.error("No se ha podido consultar el gestor: " + e.getMessage());
        }

        return managerDto;
    }

    public List<MicroserviceManagerUserDto> getUserByManager(Long managerId, List<Long> profiles) {

        List<MicroserviceManagerUserDto> users = new ArrayList<>();

        try {
            users = managerClient.findUsersByManager(managerId, profiles);
        } catch (Exception e) {
            log.error("No se ha podido consultar el gestor: " + e.getMessage());
        }

        return users;
    }

    public boolean userManagerIsDirector(Long userCode) {

        boolean isDirector = false;

        try {

            List<MicroserviceManagerProfileDto> managerProfiles = managerClient.findProfilesByUser(userCode);

            MicroserviceManagerProfileDto profileDirector = managerProfiles.stream()
                    .filter(profileDto -> profileDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR)).findAny()
                    .orElse(null);

            if (profileDirector != null) {
                isDirector = true;
            }

        } catch (FeignException e) {
            log.error("No se ha podido verificar si el usuario es un director(gestor): " + e.getMessage());
        }

        return isDirector;
    }

    public List<MicroserviceOperatorDto> getOperatorsByManager(Long managerCode) {

        List<WorkspaceOperatorEntity> workspacesOperators =
                workspaceOperatorService.getWorkspacesOperatorsByManagerCode(managerCode);

        List<Long> operatorsId = new ArrayList<>();

        for (WorkspaceOperatorEntity workspaceOperatorEntity : workspacesOperators) {
            Long operatorId = workspaceOperatorEntity.getOperatorCode();
            if (!operatorsId.contains(operatorId)) {
                operatorsId.add(operatorId);
            }
        }

        return operatorsId.stream().map(operatorBusiness::getOperatorById).collect(Collectors.toList());
    }

}
