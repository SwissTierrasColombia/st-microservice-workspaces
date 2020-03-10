package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;

public interface IIntegrationStateService {

	public IntegrationStateEntity createIntegrationState(IntegrationStateEntity integrationState);

	public Long getCount();

	public IntegrationStateEntity getIntegrationStateById(Long id);

}
