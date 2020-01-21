package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.repositories.IntegrationRepository;

@Service
public class IntegrationService implements IIntegrationService {

	@Autowired
	private IntegrationRepository integrationRepository;

	@Override
	@Transactional
	public IntegrationEntity createIntegration(IntegrationEntity integrationEntity) {
		return integrationRepository.save(integrationEntity);
	}

	@Override
	public IntegrationEntity getIntegrationByMunicipalityAndState(MunicipalityEntity municipality,
			IntegrationStateEntity state) {
		return integrationRepository.findByStateAndMunicipality(state, municipality);
	}

}
