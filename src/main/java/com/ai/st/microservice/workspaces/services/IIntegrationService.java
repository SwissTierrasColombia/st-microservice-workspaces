package com.ai.st.microservice.workspaces.services;

import java.util.List;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;

public interface IIntegrationService {

	public IntegrationEntity getIntegrationByWorkspaceAndState(WorkspaceEntity workspace, IntegrationStateEntity state);

	public IntegrationEntity createIntegration(IntegrationEntity integrationEntity);

	public List<IntegrationEntity> getIntegrationByWorkspaceAndStates(Long workspaceId, List<Long> states);

	public IntegrationEntity getIntegrationById(Long id);

	public IntegrationEntity updateIntegration(IntegrationEntity integrationEntity);

	public List<IntegrationEntity> getIntegrationByWorkspace(WorkspaceEntity workspaceEntity);

	public IntegrationEntity getIntegrationByCadastreAndSnrAndState(Long cadastreId, Long snrId,
			IntegrationStateEntity state);

}
