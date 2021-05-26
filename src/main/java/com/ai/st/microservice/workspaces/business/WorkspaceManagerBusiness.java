package com.ai.st.microservice.workspaces.business;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.dto.WorkspaceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;
import com.ai.st.microservice.workspaces.services.IWorkspaceManagerService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;

@Component
public class WorkspaceManagerBusiness {

    private final IWorkspaceManagerService workspaceManagerService;
    private final IWorkspaceService workspaceService;
    private final ManagerBusiness managerBusiness;

    public WorkspaceManagerBusiness(IWorkspaceManagerService workspaceManagerService, IWorkspaceService workspaceService,
                                    ManagerBusiness managerBusiness) {
        this.workspaceManagerService = workspaceManagerService;
        this.workspaceService = workspaceService;
        this.managerBusiness = managerBusiness;
    }

    public WorkspaceManagerDto createWorkspaceManager(Long managerCode, String specificObservations,
                                                      String generalObservations, Date startDate, String urlSupportFile, Long workspaceId) {

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);

        WorkspaceManagerEntity newManager = new WorkspaceManagerEntity();
        newManager.setManagerCode(managerCode);
        if (specificObservations != null && !specificObservations.isEmpty()) {
            newManager.setObservations(specificObservations);
        } else {
            newManager.setObservations(generalObservations);
        }
        newManager.setStartDate(startDate);
        newManager.setSupportFile(urlSupportFile);
        newManager.setWorkspace(workspaceEntity);

        newManager = workspaceManagerService.createManager(newManager);

        return entityParseToDto(newManager);
    }

    public WorkspaceManagerDto updateWorkspaceManager(Long workspaceManagerId, String observations, Date startDate) {

        WorkspaceManagerEntity workspaceManagerEntity = workspaceManagerService
                .getWorkspaceManagerById(workspaceManagerId);

        workspaceManagerEntity.setObservations(observations);
        workspaceManagerEntity.setStartDate(startDate);

        workspaceManagerEntity = workspaceManagerService.updateWorkspaceManager((workspaceManagerEntity));

        return entityParseToDto(workspaceManagerEntity);
    }

    public void deleteWorkspaceManagerById(Long workspaceManagerId) {
        workspaceManagerService.deleteWorkspaceManagerById(workspaceManagerId);
    }

    public WorkspaceManagerDto entityParseToDto(WorkspaceManagerEntity workspaceManagerEntity) {

        WorkspaceManagerDto workspaceManagerDto = new WorkspaceManagerDto();

        workspaceManagerDto.setManagerCode(workspaceManagerEntity.getManagerCode());
        workspaceManagerDto.setObservations(workspaceManagerEntity.getObservations());
        workspaceManagerDto.setStartDate(workspaceManagerEntity.getStartDate());

        try {
            MicroserviceManagerDto managerDto = managerBusiness.getManagerById(workspaceManagerEntity.getManagerCode());
            workspaceManagerDto.setManager(managerDto);
        } catch (Exception e) {
            workspaceManagerDto.setManager(null);
        }

        return workspaceManagerDto;
    }

}
