package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.repositories.IntegrationStateRepository;

@Service
public class IntegrationStateService implements IIntegrationStateService {

	@Autowired
	private IntegrationStateRepository integrationStateRepository;

	@Override
	@Transactional
	public IntegrationStateEntity createIntegrationState(IntegrationStateEntity integrationState) {
		return integrationStateRepository.save(integrationState);
	}

	@Override
	public Long getCount() {
		return integrationStateRepository.count();
	}

	@Override
	public IntegrationStateEntity getIntegrationStateById(Long id) {
		return integrationStateRepository.findById(id).orElse(null);
	}

}
