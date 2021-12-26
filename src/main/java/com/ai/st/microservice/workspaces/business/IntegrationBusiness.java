package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.ProviderFeignClient;
import com.ai.st.microservice.common.clients.SupplyFeignClient;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.clients.GeovisorFeignClient;
import com.ai.st.microservice.workspaces.dto.IntegrationDto;
import com.ai.st.microservice.workspaces.dto.IntegrationHistoryDto;
import com.ai.st.microservice.workspaces.dto.IntegrationStatDto;
import com.ai.st.microservice.workspaces.dto.IntegrationStateDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.geovisor.MicroserviceDataMapDto;
import com.ai.st.microservice.workspaces.dto.geovisor.MicroserviceSetupMapDto;
import com.ai.st.microservice.workspaces.dto.supplies.CustomSupplyDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationHistoryEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStatEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;
import com.ai.st.microservice.workspaces.services.IIntegrationService;
import com.ai.st.microservice.workspaces.services.IIntegrationStatService;
import com.ai.st.microservice.workspaces.services.IIntegrationStateService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IntegrationBusiness {

    private final SupplyFeignClient supplyClient;
    private final ProviderFeignClient providerClient;
    private final GeovisorFeignClient geovisorClient;
    private final IIntegrationService integrationService;
    private final IIntegrationStatService integrationStatService;
    private final IIntegrationStateService integrationStateService;
    private final IWorkspaceService workspaceService;
    private final SupplyBusiness supplyBusiness;
    private final MunicipalityBusiness municipalityBusiness;
    private final DatabaseIntegrationBusiness databaseBusiness;
    private final CrytpoBusiness cryptoBusiness;

    private final Logger log = LoggerFactory.getLogger(IntegrationBusiness.class);

    public IntegrationBusiness(SupplyFeignClient supplyClient, ProviderFeignClient providerClient, GeovisorFeignClient geovisorClient,
                               IIntegrationService integrationService, IIntegrationStatService integrationStatService,
                               IIntegrationStateService integrationStateService, IWorkspaceService workspaceService,
                               SupplyBusiness supplyBusiness, MunicipalityBusiness municipalityBusiness,
                               DatabaseIntegrationBusiness databaseBusiness, CrytpoBusiness cryptoBusiness) {
        this.supplyClient = supplyClient;
        this.providerClient = providerClient;
        this.geovisorClient = geovisorClient;
        this.integrationService = integrationService;
        this.integrationStatService = integrationStatService;
        this.integrationStateService = integrationStateService;
        this.workspaceService = workspaceService;
        this.supplyBusiness = supplyBusiness;
        this.municipalityBusiness = municipalityBusiness;
        this.databaseBusiness = databaseBusiness;
        this.cryptoBusiness = cryptoBusiness;
    }

    public IntegrationDto createIntegration(String hostname, String port, String database, String schema,
                                            String username, String password, Long supplyCadastreId, Long supplySnrId, Long supplyAntId,
                                            WorkspaceEntity workspaceEntity, IntegrationStateEntity stateEntity, Long userCode, Long managerCode,
                                            String user) {

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
        integrationEntity.setManagerCode(managerCode);

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
        if (integrationEntity == null) {
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
        if (integrationEntity == null) {
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

    public IntegrationDto updateStateToIntegration(Long integrationId, Long stateId, String errors, Long userCode, Long managerCode,
                                                   String user) throws BusinessException {

        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
        if (integrationEntity == null) {
            throw new BusinessException("No se ha encontrado la integración");
        }

        IntegrationStateEntity stateEntity = integrationStateService.getIntegrationStateById(stateId);
        if (stateEntity == null) {
            throw new BusinessException("No se ha encontrado el estado de integración");
        }

        integrationEntity.setErrors(errors);

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
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        if (managerCode != null) {
            WorkspaceEntity workspaceActive = workspaceService
                    .getWorkspaceActiveByMunicipality(workspaceEntity.getMunicipality());
            if (workspaceActive != null) {

                WorkspaceManagerEntity workspaceManagerFound = workspaceActive.getManagers().stream()
                        .filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
                if (workspaceManagerFound == null) {
                    throw new BusinessException("No tiene acceso al municipio.");
                }

            }
        }

        List<IntegrationEntity> listIntegrationsEntity =
                integrationService.getIntegrationByWorkspace(workspaceEntity, managerCode);

        for (IntegrationEntity integrationEntity : listIntegrationsEntity) {
            listIntegrationsDto.add(this.transformEntityToDto(integrationEntity));
        }

        for (IntegrationDto integrationDto : listIntegrationsDto) {

            try {

                MicroserviceSupplyDto responseCadastre = supplyClient.findSupplyById(integrationDto.getSupplyCadastreId());
                CustomSupplyDto supplyCadastreDto = new CustomSupplyDto(responseCadastre);

                supplyCadastreDto.setTypeSupply(providerClient.findTypeSuppleById(supplyCadastreDto.getTypeSupplyCode()));
                integrationDto.setSupplyCadastre(supplyCadastreDto);

                MicroserviceSupplyDto responseSnr = supplyClient.findSupplyById(integrationDto.getSupplySnrId());
                CustomSupplyDto supplySnrDto = new CustomSupplyDto(responseSnr);

                supplySnrDto.setTypeSupply(providerClient.findTypeSuppleById(supplySnrDto.getTypeSupplyCode()));
                integrationDto.setSupplySnr(supplySnrDto);

            } catch (Exception ignored) {

            }

        }

        return listIntegrationsDto;
    }

    public void deleteIntegration(Long integrationId) throws BusinessException {

        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
        if (integrationEntity == null) {
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
        integrationDto.setUrlMap(integrationEntity.getUrlMap());
        integrationDto.setManagerCode(integrationEntity.getManagerCode());
        integrationDto.setErrors(integrationEntity.getErrors());

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
            log.error("Error consultando municipio: " + e.getMessage());
        }

        return integrationDto;
    }

    public List<IntegrationDto> getIntegrationsRunning(MicroserviceManagerDto managerDto) throws BusinessException {

        List<IntegrationDto> listIntegrationsDto = new ArrayList<>();

        List<WorkspaceEntity> workspacesEntity = workspaceService.getWorkspacesByManager(managerDto.getId());

        List<IntegrationEntity> listIntegrationsEntity = integrationService
                .getIntegrationsByWorkspaces(workspacesEntity, managerDto.getId());

        for (IntegrationEntity integrationEntity : listIntegrationsEntity) {
            listIntegrationsDto.add(this.transformEntityToDto(integrationEntity));
        }

        for (IntegrationDto integrationDto : listIntegrationsDto) {

            try {

                CustomSupplyDto supplyCadastreDto = supplyBusiness
                        .getSupplyById(integrationDto.getSupplyCadastreId());
                integrationDto.setSupplyCadastre(supplyCadastreDto);

                CustomSupplyDto supplySnrDto = supplyBusiness.getSupplyById(integrationDto.getSupplySnrId());
                integrationDto.setSupplySnr(supplySnrDto);

            } catch (Exception e) {
                log.error("Error consultando insumo: " + e.getMessage());
            }

        }

        return listIntegrationsDto;
    }

    public IntegrationDto updateURLMap(Long integrationId, String url) throws BusinessException {

        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
        if (integrationEntity == null) {
            throw new BusinessException("No se ha encontrado la integración");
        }

        integrationEntity.setUrlMap(url);

        integrationEntity = integrationService.updateIntegration(integrationEntity);

        return this.transformEntityToDto(integrationEntity);
    }

    public void configureViewIntegration(Long integrationId, Long managerId) throws BusinessException {

        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
        if (integrationEntity == null) {
            throw new BusinessException("La integración no existe");
        }

        if (!integrationEntity.getState().getId().equals(IntegrationStateBusiness.STATE_GENERATED_PRODUCT)
                || integrationEntity.getUrlMap() != null) {
            throw new BusinessException(
                    "No se puede configurar el geovisor porque la integración esta en un estado inválido.");
        }

        WorkspaceManagerEntity workspaceManagerEntity =
                integrationEntity.getWorkspace().getManagers().stream().filter(m -> m.getManagerCode().equals(managerId)).findAny().orElse(null);
        if (workspaceManagerEntity == null) {
            throw new BusinessException("La integración no pertenece al gestor");
        }

        try {

            String host = cryptoBusiness.decrypt(integrationEntity.getHostname());
            String port = cryptoBusiness.decrypt(integrationEntity.getPort());
            String database = cryptoBusiness.decrypt(integrationEntity.getDatabase());
            String schema = cryptoBusiness.decrypt(integrationEntity.getSchema());
            String username = cryptoBusiness.decrypt(integrationEntity.getUsername());
            String password = cryptoBusiness.decrypt(integrationEntity.getPassword());
            String nameView = "integration_cat_reg";

            databaseBusiness.createViewIntegration(host, port, database, schema, nameView);

            MicroserviceSetupMapDto data = new MicroserviceSetupMapDto();
            data.setName_conn(nameView);
            data.setDbname(database);
            data.setHost(host);
            data.setPassword(password);
            data.setPort(Integer.parseInt(port));
            data.setSchema(schema);
            data.setUser(username);
            data.setLayers(new ArrayList<>(Collections.singletonList(nameView)));

            MicroserviceDataMapDto dataResponse = geovisorClient.setupMap(data);

            log.info("URL: " + dataResponse.getData());

            String url = dataResponse.getData();

            updateURLMap(integrationEntity.getId(), url);

        } catch (Exception e) {
            throw new BusinessException(
                    "No se ha podido realizar la configuración de la integración para su visualización.");
        }

    }

}