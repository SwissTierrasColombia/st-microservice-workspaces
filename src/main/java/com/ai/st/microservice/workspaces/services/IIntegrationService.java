package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;

public interface IIntegrationService {

	public IntegrationEntity getIntegrationByMunicipalityAndState(MunicipalityEntity municipality,
			IntegrationStateEntity state);

	public IntegrationEntity createIntegration(IntegrationEntity integrationEntity);

}
