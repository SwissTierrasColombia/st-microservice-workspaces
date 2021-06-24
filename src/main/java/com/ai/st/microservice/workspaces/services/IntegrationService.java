package com.ai.st.microservice.workspaces.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.repositories.IntegrationRepository;

@Service
public class IntegrationService implements IIntegrationService {

    private final IntegrationRepository integrationRepository;

    public IntegrationService(IntegrationRepository integrationRepository) {
        this.integrationRepository = integrationRepository;
    }

    @Override
    @Transactional
    public IntegrationEntity createIntegration(IntegrationEntity integrationEntity) {
        return integrationRepository.save(integrationEntity);
    }

    @Override
    public IntegrationEntity getIntegrationByWorkspaceAndState(WorkspaceEntity workspace,
                                                               IntegrationStateEntity state) {
        return integrationRepository.findByWorkspaceAndState(workspace, state);
    }

    @Override
    public List<IntegrationEntity> getIntegrationByWorkspaceAndStates(Long workspaceId, List<Long> states) {
        return integrationRepository.findIntegrationsByWorkspaceAndStates(workspaceId, states);
    }

    @Override
    public IntegrationEntity getIntegrationById(Long id) {
        return integrationRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public IntegrationEntity updateIntegration(IntegrationEntity integrationEntity) {
        return integrationRepository.save(integrationEntity);
    }

    @Override
    public List<IntegrationEntity> getIntegrationByWorkspace(WorkspaceEntity workspaceEntity, Long managerCode) {
        return integrationRepository.findByWorkspaceAndManagerCode(workspaceEntity, managerCode);
    }

    @Override
    public IntegrationEntity getIntegrationByCadastreAndSnrAndState(Long cadastreId, Long snrId,
                                                                    IntegrationStateEntity state, Long managerCode) {
        return integrationRepository.findBySupplyCadastreIdAndSupplySnrIdAndStateAndManagerCode(cadastreId, snrId, state, managerCode);
    }

    @Override
    @Transactional
    public void deleteIntegration(Long id) {
        integrationRepository.deleteById(id);
    }

    @Override
    public List<IntegrationEntity> getIntegrationsByWorkspaces(List<WorkspaceEntity> workspaces, Long managerCode) {
        return integrationRepository.findByWorkspaceInAndManagerCode(workspaces, managerCode);
    }

    @Override
    public List<IntegrationEntity> getPendingIntegrations(Long workspaceId, List<Long> states, Long managerCode) {
        return integrationRepository.findIntegrationsPending(workspaceId, states, managerCode);
    }

}
