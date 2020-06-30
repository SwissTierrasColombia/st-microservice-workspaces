package com.ai.st.microservice.workspaces.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
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
	public List<IntegrationEntity> getIntegrationByWorkspace(WorkspaceEntity workspaceEntity) {
		return integrationRepository.findByWorkspace(workspaceEntity);
	}

	@Override
	public IntegrationEntity getIntegrationByCadastreAndSnrAndState(Long cadastreId, Long snrId,
			IntegrationStateEntity state) {
		return integrationRepository.findBySupplyCadastreIdAndSupplySnrIdAndState(cadastreId, snrId, state);
	}

	@Override
	@Transactional
	public void deleteIntegration(Long id) {
		integrationRepository.deleteById(id);
	}

	@Override
	public List<IntegrationEntity> getIntegrationsByWorkspaces(List<WorkspaceEntity> workspaces) {
		return integrationRepository.findByWorkspaceIn(workspaces);
	}

}
