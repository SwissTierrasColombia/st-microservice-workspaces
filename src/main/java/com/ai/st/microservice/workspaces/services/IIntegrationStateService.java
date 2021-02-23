package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;

public interface IIntegrationStateService {

    IntegrationStateEntity createIntegrationState(IntegrationStateEntity integrationState);

    Long getCount();

    IntegrationStateEntity getIntegrationStateById(Long id);

}
