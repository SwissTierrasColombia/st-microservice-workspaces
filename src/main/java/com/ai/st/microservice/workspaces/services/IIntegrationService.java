package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;

public interface IIntegrationService {

	public IntegrationEntity getIntegrationByMunicipalityActive(MunicipalityEntity municipality);

}
