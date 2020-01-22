package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.IntegrationStatEntity;
import com.ai.st.microservice.workspaces.repositories.IntegrationStatRepository;

@Service
public class IntegrationStatService implements IIntegrationStatService {

	@Autowired
	private IntegrationStatRepository integrationStatRepository;

	@Override
	@Transactional
	public IntegrationStatEntity createIntegrationStat(IntegrationStatEntity stat) {
		return integrationStatRepository.save(stat);
	}

}
