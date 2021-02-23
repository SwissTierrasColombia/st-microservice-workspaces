package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.IntegrationStateBusiness;
import com.ai.st.microservice.workspaces.business.ManagerBusiness;
import com.ai.st.microservice.workspaces.business.NotificationBusiness;
import com.ai.st.microservice.workspaces.business.RoleBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationStatDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IIntegrationService;
import com.ai.st.microservice.workspaces.services.IIntegrationStateService;

@Component
public class RabbitMQUpdateIntegrationListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IntegrationBusiness integrationBusiness;

    @Autowired
    private NotificationBusiness notificationBusiness;

    @Autowired
    private ManagerBusiness managerBusiness;

    @Autowired
    private UserBusiness userBusiness;

    @Autowired
    private IIntegrationService integrationService;

    @Autowired
    private IIntegrationStateService integrationStateService;

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

                List<MicroserviceManagerUserDto> directors = managerBusiness.getUserByManager(integrationEntity.getManagerCode(),
                        new ArrayList<>(Collections.singletonList(RoleBusiness.SUB_ROLE_DIRECTOR)));

                for (MicroserviceManagerUserDto directorDto : directors) {

                    MicroserviceUserDto userDto = userBusiness.getUserById(directorDto.getUserCode());
                    if (userDto != null) {
                        notificationBusiness.sendNotificationInputIntegrations(userDto.getEmail(), userDto.getId(),
                                integrationStatus.getName(), municipalityEntity.getName(),
                                municipalityEntity.getDepartment().getName(), integrationEntity.getStartedAt());
                    }

                }

            } catch (Exception e) {
                log.error("Error enviando notificación informando estado de la integración automática: "
                        + e.getMessage());
            }

            integrationBusiness.updateStateToIntegration(integrationStats.getIntegrationId(), stateId, null, null,
                    "SISTEMA");

        } catch (Exception e) {
            log.error("Error RabbitMQUpdateIntegrationListener@updateIntegration#Business ---> " + e.getMessage());
        }

    }

}
