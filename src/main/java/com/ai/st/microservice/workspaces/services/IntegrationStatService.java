package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.IntegrationStatEntity;
import com.ai.st.microservice.workspaces.repositories.IntegrationStatRepository;

@Service
public class IntegrationStatService implements IIntegrationStatService {

    private final IntegrationStatRepository integrationStatRepository;

    public IntegrationStatService(IntegrationStatRepository integrationStatRepository) {
        this.integrationStatRepository = integrationStatRepository;
    }

    @Override
    @Transactional
    public IntegrationStatEntity createIntegrationStat(IntegrationStatEntity stat) {
        return integrationStatRepository.save(stat);
    }

}
