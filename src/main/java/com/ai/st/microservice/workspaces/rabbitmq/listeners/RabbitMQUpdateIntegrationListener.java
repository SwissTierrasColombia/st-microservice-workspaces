package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.ili.MicroserviceIntegrationStatDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.business.RoleBusiness;

import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.IntegrationStateBusiness;
import com.ai.st.microservice.workspaces.business.ManagerMicroserviceBusiness;
import com.ai.st.microservice.workspaces.business.NotificationBusiness;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IIntegrationService;
import com.ai.st.microservice.workspaces.services.IIntegrationStateService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class RabbitMQUpdateIntegrationListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final IntegrationBusiness integrationBusiness;
    private final NotificationBusiness notificationBusiness;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final IIntegrationService integrationService;
    private final IIntegrationStateService integrationStateService;
    private final AdministrationBusiness administrationBusiness;

    public RabbitMQUpdateIntegrationListener(IntegrationBusiness integrationBusiness,
            NotificationBusiness notificationBusiness, ManagerMicroserviceBusiness managerBusiness,
            IIntegrationService integrationService, IIntegrationStateService integrationStateService,
            AdministrationBusiness administrationBusiness) {
        this.integrationBusiness = integrationBusiness;
        this.notificationBusiness = notificationBusiness;
        this.managerBusiness = managerBusiness;
        this.integrationService = integrationService;
        this.integrationStateService = integrationStateService;
        this.administrationBusiness = administrationBusiness;
    }

    @RabbitListener(queues = "${st.rabbitmq.queueUpdateIntegration.queue}", concurrency = "${st.rabbitmq.queueUpdateIntegration.concurrency}")
    public void updateIntegration(MicroserviceIntegrationStatDto integrationStats) {

        try {

            Long stateId;

            if (integrationStats.isStatus()) {
                integrationBusiness.addStatToIntegration(integrationStats.getIntegrationId(),
                        integrationStats.getCountSNR(), integrationStats.getCountGC(), (long) 0,
                        integrationStats.getCountMatch(), integrationStats.getPercentage());
                stateId = IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC;
                log.info("Integration automatic finished successful");
            } else {
                stateId = IntegrationStateBusiness.STATE_ERROR_INTEGRATION_AUTOMATIC;
                log.info("Integration automatic finished with errors");
            }

            // send notification
            try {

                IntegrationEntity integrationEntity = integrationService
                        .getIntegrationById(integrationStats.getIntegrationId());

                IntegrationStateEntity integrationStatus = integrationStateService.getIntegrationStateById(stateId);

                WorkspaceEntity workspaceEntity = integrationEntity.getWorkspace();
                MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

                List<MicroserviceManagerUserDto> directors = managerBusiness.getUserByManager(
                        integrationEntity.getManagerCode(),
                        new ArrayList<>(Collections.singletonList(RoleBusiness.SUB_ROLE_DIRECTOR_MANAGER)));

                for (MicroserviceManagerUserDto directorDto : directors) {

                    MicroserviceUserDto userDto = administrationBusiness.getUserById(directorDto.getUserCode());
                    if (userDto != null && userDto.getEnabled()) {
                        notificationBusiness.sendNotificationInputIntegrations(userDto.getEmail(), userDto.getId(),
                                integrationStatus.getName(), municipalityEntity.getName(),
                                municipalityEntity.getDepartment().getName(), integrationEntity.getStartedAt());
                    }

                }

            } catch (Exception e) {
                log.error("Error enviando notificación informando estado de la integración automática: "
                        + e.getMessage());
            }

            String logErrors = null;
            if (integrationStats.getErrors().size() > 0) {
                StringBuilder errors = new StringBuilder();
                for (String error : integrationStats.getErrors()) {
                    errors.append(error).append("\n");
                }
                logErrors = errors.toString();
            }

            integrationBusiness.updateStateToIntegration(integrationStats.getIntegrationId(), stateId, logErrors, null,
                    null, "SISTEMA");

        } catch (Exception e) {
            log.error("Error RabbitMQUpdateIntegrationListener@updateIntegration#Business ---> " + e.getMessage());
        }

    }

}
