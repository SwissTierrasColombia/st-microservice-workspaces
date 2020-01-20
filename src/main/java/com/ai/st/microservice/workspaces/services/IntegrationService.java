package com.ai.st.microservice.workspaces.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.repositories.IntegrationRepository;

@Service
public class IntegrationService implements IIntegrationService {

	@Autowired
	private IntegrationRepository integrationRepository;

	@Override
	public IntegrationEntity getIntegrationByMunicipalityActive(MunicipalityEntity municipality) {
		return integrationRepository.findByPendingAndMunicipality(true, municipality);
	}

}
