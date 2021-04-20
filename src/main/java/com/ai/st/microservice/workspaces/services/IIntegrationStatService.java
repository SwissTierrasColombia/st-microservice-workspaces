package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.IntegrationStatEntity;

public interface IIntegrationStatService {

    IntegrationStatEntity createIntegrationStat(IntegrationStatEntity stat);

}
