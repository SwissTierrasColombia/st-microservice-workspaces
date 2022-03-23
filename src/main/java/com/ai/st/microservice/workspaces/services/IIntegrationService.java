package com.ai.st.microservice.workspaces.services;

import java.util.List;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;

public interface IIntegrationService {

    IntegrationEntity getIntegrationByWorkspaceAndState(WorkspaceEntity workspace, IntegrationStateEntity state);

    IntegrationEntity createIntegration(IntegrationEntity integrationEntity);

    List<IntegrationEntity> getIntegrationByWorkspaceAndStates(Long workspaceId, List<Long> states);

    IntegrationEntity getIntegrationById(Long id);

    IntegrationEntity updateIntegration(IntegrationEntity integrationEntity);

    List<IntegrationEntity> getIntegrationByWorkspace(WorkspaceEntity workspaceEntity, Long managerCode);

    IntegrationEntity getIntegrationByCadastreAndSnrAndState(Long cadastreId, Long snrId, IntegrationStateEntity state,
            Long managerCode);

    void deleteIntegration(Long id);

    List<IntegrationEntity> getIntegrationsByWorkspaces(List<WorkspaceEntity> workspaces, Long managerCode);

    List<IntegrationEntity> getPendingIntegrations(Long workspaceId, List<Long> states, Long managerCode);

}
