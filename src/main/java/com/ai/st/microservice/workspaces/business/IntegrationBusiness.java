package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.dto.IntegrationDto;
import com.ai.st.microservice.workspaces.dto.IntegrationHistoryDto;
import com.ai.st.microservice.workspaces.dto.IntegrationStatDto;
import com.ai.st.microservice.workspaces.dto.IntegrationStateDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.PossibleIntegrationDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationHistoryEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStatEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IIntegrationService;
import com.ai.st.microservice.workspaces.services.IIntegrationStatService;
import com.ai.st.microservice.workspaces.services.IIntegrationStateService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;

@Component
public class IntegrationBusiness {

	@Autowired
	private SupplyFeignClient supplyClient;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private IIntegrationService integrationService;

	@Autowired
	private IIntegrationStatService integrationStatService;

	@Autowired
	private IIntegrationStateService integrationStateService;

	@Autowired
	private IWorkspaceService workspaceService;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private MunicipalityBusiness municipalityBusiness;

	public IntegrationDto createIntegration(String hostname, String port, String database, String schema,
			String username, String password, Long supplyCadastreId, Long supplySnrId, Long supplyAntId,
			WorkspaceEntity workspaceEntity, IntegrationStateEntity stateEntity, Long userCode, Long managerCode,
			String user) throws BusinessException {

		IntegrationEntity integrationEntity = new IntegrationEntity();
		integrationEntity.setDatabase(database);
		integrationEntity.setHostname(hostname);
		integrationEntity.setWorkspace(workspaceEntity);
		integrationEntity.setPassword(password);
		integrationEntity.setState(stateEntity);
		integrationEntity.setPort(port);
		integrationEntity.setSchema(schema);
		integrationEntity.setStartedAt(new Date());
		integrationEntity.setUsername(username);
		integrationEntity.setSupplyAntId(supplyAntId);
		integrationEntity.setSupplyCadastreId(supplyCadastreId);
		integrationEntity.setSupplySnrId(supplySnrId);

		// set history
		IntegrationHistoryEntity historyEntity = new IntegrationHistoryEntity();
		historyEntity.setIntegration(integrationEntity);
		historyEntity.setCreatedAt(integrationEntity.getStartedAt());
		historyEntity.setState(stateEntity);
		historyEntity.setUserCode(userCode);
		historyEntity.setManagerCode(managerCode);
		historyEntity.setUserName(user);
		integrationEntity.getHistories().add(historyEntity);

		integrationEntity = integrationService.createIntegration(integrationEntity);

		return this.transformEntityToDto(integrationEntity);
	}

	public IntegrationDto updateCredentialsIntegration(Long integrationId, String hostname, String port,
			String database, String schema, String username, String password) throws BusinessException {

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
			throw new BusinessException("No se ha encontrado la integración");
		}

		integrationEntity.setDatabase(database);
		integrationEntity.setHostname(hostname);
		integrationEntity.setPassword(password);
		integrationEntity.setPort(port);
		integrationEntity.setSchema(schema);
		integrationEntity.setStartedAt(new Date());
		integrationEntity.setUsername(username);

		integrationEntity = integrationService.updateIntegration(integrationEntity);

		return this.transformEntityToDto(integrationEntity);
	}

	public IntegrationDto addStatToIntegration(Long integrationId, Long countSnr, Long countCadastre, Long countAnt,
			Long countMatch, Double percentage) throws BusinessException {

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
			throw new BusinessException("No se ha encontrado la integración");
		}

		IntegrationStatEntity statEntity = new IntegrationStatEntity();
		statEntity.setCreatedAt(new Date());
		statEntity.setCadastreRecordsNumber(countCadastre);
		statEntity.setAntRecordsNumber(countAnt);
		statEntity.setPercentage(percentage);
		statEntity.setMatchNumber(countMatch);
		statEntity.setSnrRecordsNumber(countSnr);
		statEntity.setIntegration(integrationEntity);
		integrationStatService.createIntegrationStat(statEntity);

		return this.transformEntityToDto(integrationEntity);
	}

	public IntegrationDto updateStateToIntegration(Long integrationId, Long stateId, Long userCode, Long managerCode,
			String user) throws BusinessException {

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
			throw new BusinessException("No se ha encontrado la integración");
		}

		IntegrationStateEntity stateEntity = integrationStateService.getIntegrationStateById(stateId);
		if (!(stateEntity instanceof IntegrationStateEntity)) {
			throw new BusinessException("No se ha encontrado el estado de integración");
		}

		// new integration history
		IntegrationHistoryEntity historyEntity = new IntegrationHistoryEntity();
		historyEntity.setIntegration(integrationEntity);
		historyEntity.setCreatedAt(new Date());
		historyEntity.setState(stateEntity);
		historyEntity.setUserCode(userCode);
		historyEntity.setManagerCode(managerCode);
		historyEntity.setUserName(user);

		integrationEntity.getHistories().add(historyEntity);
		integrationEntity.setState(stateEntity);

		integrationEntity = integrationService.updateIntegration(integrationEntity);

		return this.transformEntityToDto(integrationEntity);
	}

	public List<IntegrationDto> getIntegrationsByWorkspace(Long workspaceId, Long managerCode)
			throws BusinessException {

		List<IntegrationDto> listIntegrationsDto = new ArrayList<>();

		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		if (managerCode != null) {
			WorkspaceEntity workspaceActive = workspaceService
					.getWorkspaceActiveByMunicipality(workspaceEntity.getMunicipality());
			if (workspaceActive instanceof WorkspaceEntity) {
				if (!managerCode.equals(workspaceActive.getManagerCode())) {
					throw new BusinessException("No tiene acceso al municipio.");
				}
			}
		}

		List<IntegrationEntity> listIntegrationsEntity = integrationService.getIntegrationByWorkspace(workspaceEntity);

		for (IntegrationEntity integrationEntity : listIntegrationsEntity) {
			listIntegrationsDto.add(this.transformEntityToDto(integrationEntity));
		}

		for (IntegrationDto integrationDto : listIntegrationsDto) {

			try {
				MicroserviceSupplyDto supplyCadastreDto = supplyClient
						.findSupplyById(integrationDto.getSupplyCadastreId());
				supplyCadastreDto
						.setTypeSupply(providerClient.findTypeSuppleById(supplyCadastreDto.getTypeSupplyCode()));
				integrationDto.setSupplyCadastre(supplyCadastreDto);

				MicroserviceSupplyDto supplySnrDto = supplyClient.findSupplyById(integrationDto.getSupplySnrId());
				supplySnrDto.setTypeSupply(providerClient.findTypeSuppleById(supplySnrDto.getTypeSupplyCode()));
				integrationDto.setSupplySnr(supplySnrDto);

			} catch (Exception e) {

			}

		}

		return listIntegrationsDto;
	}

	public void deleteIntegration(Long integrationId) throws BusinessException {

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
			throw new BusinessException("No se ha encontrado la integración");
		}

		try {
			integrationService.deleteIntegration(integrationId);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido eliminar la integración.");
		}

	}

	private IntegrationDto transformEntityToDto(IntegrationEntity integrationEntity) {

		IntegrationDto integrationDto = new IntegrationDto();
		integrationDto.setId(integrationEntity.getId());
		integrationDto.setStartedAt(integrationEntity.getStartedAt());
		integrationDto.setFinishedAt(integrationEntity.getFinishedAt());
		integrationDto.setHostname(integrationEntity.getHostname());
		integrationDto.setDatabase(integrationEntity.getDatabase());
		integrationDto.setPort(integrationEntity.getPort());
		integrationDto.setPassword(integrationEntity.getPassword());
		integrationDto.setUsername(integrationEntity.getUsername());
		integrationDto.setSchema(integrationEntity.getSchema());
		integrationDto.setSupplyAntId(integrationEntity.getSupplyAntId());
		integrationDto.setSupplyCadastreId(integrationEntity.getSupplyCadastreId());
		integrationDto.setSupplySnrId(integrationEntity.getSupplySnrId());

		IntegrationStateEntity integrationStateEntity = integrationEntity.getState();
		integrationDto.setIntegrationState(new IntegrationStateDto(integrationStateEntity.getId(),
				integrationStateEntity.getName(), integrationStateEntity.getDescription()));

		List<IntegrationStatEntity> listStatsEntity = integrationEntity.getStats();
		if (listStatsEntity.size() > 0) {
			for (IntegrationStatEntity statEntity : listStatsEntity) {
				IntegrationStatDto statDto = new IntegrationStatDto();
				statDto.setId(statEntity.getId());
				statDto.setAntRecordsNumber(statEntity.getAntRecordsNumber());
				statDto.setCadastreRecordsNumber(statEntity.getCadastreRecordsNumber());
				statDto.setSnrRecordsNumber(statEntity.getSnrRecordsNumber());
				statDto.setPercentage(statEntity.getPercentage());
				statDto.setMatchNumber(statEntity.getMatchNumber());
				statDto.setCreatedAt(statEntity.getCreatedAt());

				integrationDto.getStats().add(statDto);
			}
		}

		List<IntegrationHistoryEntity> histories = integrationEntity.getHistories();
		if (histories.size() > 0) {
			for (IntegrationHistoryEntity historyEntity : histories) {
				IntegrationHistoryDto historyDto = new IntegrationHistoryDto();
				historyDto.setId(historyEntity.getId());
				historyDto.setCreatedAt(historyEntity.getCreatedAt());
				IntegrationStateEntity stateHistoryEntity = historyEntity.getState();
				historyDto.setState(new IntegrationStateDto(stateHistoryEntity.getId(), stateHistoryEntity.getName(),
						stateHistoryEntity.getDescription()));
				historyDto.setUserName(historyEntity.getUserName());

				integrationDto.getHistories().add(historyDto);

			}
		}

		try {
			MunicipalityDto municipalityDto = municipalityBusiness
					.getMunicipalityByCode(integrationEntity.getWorkspace().getMunicipality().getCode());
			if (municipalityDto != null) {
				integrationDto.setMunicipalityDto(municipalityDto);
			}
		} catch (Exception e) {

		}

		return integrationDto;
	}

	public List<IntegrationDto> getIntegrationsRunning(MicroserviceManagerDto managerDto) throws BusinessException {

		List<IntegrationDto> listIntegrationsDto = new ArrayList<>();

		List<WorkspaceEntity> workspacesEntity = workspaceService.getWorkspacesByManagerAndIsActive(managerDto.getId(),
				true);

		List<IntegrationEntity> listIntegrationsEntity = integrationService
				.getIntegrationsByWorkspaces(workspacesEntity);

		for (IntegrationEntity integrationEntity : listIntegrationsEntity) {
			listIntegrationsDto.add(this.transformEntityToDto(integrationEntity));
		}

		for (IntegrationDto integrationDto : listIntegrationsDto) {

			try {

				MicroserviceSupplyDto supplyCadastreDto = supplyBusiness
						.getSupplyById(integrationDto.getSupplyCadastreId());
				integrationDto.setSupplyCadastre(supplyCadastreDto);

				MicroserviceSupplyDto supplySnrDto = supplyBusiness.getSupplyById(integrationDto.getSupplySnrId());
				integrationDto.setSupplySnr(supplySnrDto);

			} catch (Exception e) {

			}

		}

		return listIntegrationsDto;
	}

	public List<PossibleIntegrationDto> getPossiblesIntegrations(MicroserviceManagerDto managerDto)
			throws BusinessException {

		List<PossibleIntegrationDto> listIntegrationsDto = new ArrayList<>();

		List<WorkspaceEntity> workspacesEntity = workspaceService.getWorkspacesByManagerAndIsActive(managerDto.getId(),
				true);

		for (WorkspaceEntity workspaceEntity : workspacesEntity) {

			MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

			@SuppressWarnings("unchecked")
			List<MicroserviceSupplyDto> suppliesDto = (List<MicroserviceSupplyDto>) supplyBusiness
					.getSuppliesByMunicipalityManager(municipalityEntity.getId(), managerDto.getId(), null, null, null, true);

			MicroserviceSupplyDto supplyCadastralDto = null;
			try {
				supplyCadastralDto = suppliesDto.stream()
						.filter(s -> s.getTypeSupply().getProvider().getProviderCategory().getId().equals((long) 1))
						.findAny().orElse(null);
			} catch (Exception e) {

			}

			if (supplyCadastralDto != null) {

				MicroserviceSupplyDto supplyRegistralDto = null;
				try {
					supplyRegistralDto = suppliesDto.stream()
							.filter(s -> s.getTypeSupply().getProvider().getProviderCategory().getId().equals((long) 2))
							.findAny().orElse(null);
				} catch (Exception e) {

				}

				MicroserviceSupplyDto supplyAntDto = null;
				try {
					supplyAntDto = suppliesDto.stream()
							.filter(s -> s.getTypeSupply().getProvider().getProviderCategory().getId().equals((long) 3))
							.findAny().orElse(null);
				} catch (Exception e) {

				}

				if (supplyRegistralDto != null || supplyAntDto != null) {

					MunicipalityDto municipalityDto = municipalityBusiness
							.getMunicipalityByCode(municipalityEntity.getCode());

					listIntegrationsDto.add(new PossibleIntegrationDto(municipalityDto));
				}

			}

		}

		return listIntegrationsDto;
	}

}
