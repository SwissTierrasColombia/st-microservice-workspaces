package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.ili.MicroserviceIliExportResultDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceCreateSupplyAttachmentDto;
import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.business.RoleBusiness;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.supplies.CustomSupplyDto;
import com.ai.st.microservice.workspaces.business.CrytpoBusiness;
import com.ai.st.microservice.workspaces.business.DatabaseIntegrationBusiness;
import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.IntegrationStateBusiness;
import com.ai.st.microservice.workspaces.business.ManagerMicroserviceBusiness;
import com.ai.st.microservice.workspaces.business.NotificationBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IntegrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RabbitMQUpdateExportIntegrationListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${st.filesDirectory}")
    private String stFilesDirectory;

    private final IntegrationBusiness integrationBusiness;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final NotificationBusiness notificationBusiness;
    private final DatabaseIntegrationBusiness databaseIntegration;
    private final CrytpoBusiness cryptoBusiness;
    private final SupplyBusiness supplyBusiness;
    private final IntegrationService integrationService;
    private final AdministrationBusiness administrationBusiness;

    public RabbitMQUpdateExportIntegrationListener(IntegrationBusiness integrationBusiness, ManagerMicroserviceBusiness managerBusiness,
                                                   NotificationBusiness notificationBusiness, DatabaseIntegrationBusiness databaseIntegration,
                                                   CrytpoBusiness cryptoBusiness, SupplyBusiness supplyBusiness, IntegrationService integrationService,
                                                   AdministrationBusiness administrationBusiness) {
        this.integrationBusiness = integrationBusiness;
        this.managerBusiness = managerBusiness;
        this.notificationBusiness = notificationBusiness;
        this.databaseIntegration = databaseIntegration;
        this.cryptoBusiness = cryptoBusiness;
        this.supplyBusiness = supplyBusiness;
        this.integrationService = integrationService;
        this.administrationBusiness = administrationBusiness;
    }

    @RabbitListener(queues = "${st.rabbitmq.queueUpdateExport.queue}", concurrency = "${st.rabbitmq.queueUpdateExport.concurrency}")
    public void updateExport(MicroserviceIliExportResultDto resultExportDto) {

        try {

            Long stateId;

            IntegrationEntity integrationEntity = integrationService
                    .getIntegrationById(resultExportDto.getIntegrationId());

            if (integrationEntity != null) {

                WorkspaceEntity workspaceEntity = integrationEntity.getWorkspace();
                MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

                if (resultExportDto.isStatus()) {
                    stateId = IntegrationStateBusiness.STATE_GENERATED_PRODUCT;
                    log.info("Export finished successful");

                    if (resultExportDto.getStats() != null) {
                        integrationBusiness.addStatToIntegration(integrationEntity.getId(),
                                resultExportDto.getStats().getCountSNR(), resultExportDto.getStats().getCountGC(),
                                (long) 0, resultExportDto.getStats().getCountMatch(),
                                resultExportDto.getStats().getPercentage());
                    }

                    String municipalityCode = municipalityEntity.getCode();

                    String urlDocumentaryRepository = resultExportDto.getPathFile();

                    log.info("saving url file: " + urlDocumentaryRepository);

                    List<MicroserviceCreateSupplyAttachmentDto> attachments = new ArrayList<>();
                    attachments.add(new MicroserviceCreateSupplyAttachmentDto(urlDocumentaryRepository,
                            SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY));

                    // load supply to municipality
                    String observations = "Archivo XTF generado para el modelo de insumos";

                    CustomSupplyDto supplyCadastralDto = supplyBusiness.getSupplyById(integrationEntity.getSupplyCadastreId());
                    CustomSupplyDto supplyRegistralDto = supplyBusiness.getSupplyById(integrationEntity.getSupplySnrId());
                    boolean isValid = supplyCadastralDto.getValid() && supplyRegistralDto.getValid();

                    supplyBusiness.createSupply(municipalityCode, observations, null, integrationEntity.getManagerCode(), attachments, null,
                            null, null, integrationEntity.getManagerCode(), null, resultExportDto.getModelVersion(),
                            SupplyBusiness.SUPPLY_STATE_ACTIVE, "Datos en modelo de insumos para el Municipio", isValid);

                    /*
                     * try { // delete database
                     * databaseIntegration.dropDatabase(cryptoBusiness.decrypt(integrationEntity.
                     * getDatabase()), cryptoBusiness.decrypt(integrationEntity.getUsername())); }
                     * catch (Exception e) { log.error("No se ha podido borrar la base de datos: " +
                     * e.getMessage()); }
                     */

                    // integrationBusiness.configureViewIntegration(integrationEntity.getId(), integrationEntity.getManagerCode());

                    // send notification

                    try {

                        List<MicroserviceManagerUserDto> directors = managerBusiness.getUserByManager(integrationEntity.getManagerCode(),
                                new ArrayList<>(Collections.singletonList(RoleBusiness.SUB_ROLE_DIRECTOR)));

                        for (MicroserviceManagerUserDto directorDto : directors) {

                            MicroserviceUserDto userDto = administrationBusiness.getUserById(directorDto.getUserCode());
                            if (userDto != null && userDto.getEnabled()) {
                                notificationBusiness.sendNotificationProductGenerated(userDto.getEmail(),
                                        userDto.getId(), municipalityEntity.getName(),
                                        municipalityEntity.getDepartment().getName(), new Date());
                            }

                        }

                    } catch (Exception e) {
                        log.error("Error enviando notificación de producto generado: " + e.getMessage());
                    }

                } else {

                    stateId = IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT;
                    log.error("Export finished with errors");
                }

                String logErrors = null;
                if (resultExportDto.getErrors().size() > 0) {
                    StringBuilder errors = new StringBuilder();
                    for (String error : resultExportDto.getErrors()) {
                        errors.append(error).append("\n");
                    }
                    logErrors = errors.toString();
                }

                integrationBusiness.updateStateToIntegration(resultExportDto.getIntegrationId(), stateId, logErrors, null, null, "SISTEMA");

            }

        } catch (Exception e) {
            log.info("Error update export integration: " + e.getMessage());

            Long stateId = IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT;
            try {
                integrationBusiness.updateStateToIntegration(resultExportDto.getIntegrationId(), stateId, e.getMessage(), null, null, "SISTEMA");
            } catch (BusinessException e1) {
                log.error("Error actualizando el estado de la integración por error: " + e.getMessage());
            }
        }

    }

}
