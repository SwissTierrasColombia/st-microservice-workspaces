package com.ai.st.microservice.workspaces.business;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.NotifierFeignClient;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationAssignmentOperationMunicipalityDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationDeliveryOfInputsDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationInputIntegrationsDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationInputRequestDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationIntegrationFileGenerationDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationLoadOfInputsDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationMunicipalityManagementDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationNewUserDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationTaskAssignmentDto;

@Component
public class NotificationBusiness {

    private final Logger log = LoggerFactory.getLogger(NotificationBusiness.class);

    private final NotifierFeignClient notifierClient;

    public NotificationBusiness(NotifierFeignClient notifierClient) {
        this.notifierClient = notifierClient;
    }

    public void sendNotificationCreationUser(String email, String password, String profile, String user,
                                             Long userCode) {

        try {

            MicroserviceNotificationNewUserDto notification = new MicroserviceNotificationNewUserDto();
            notification.setEmail(email);
            notification.setPassword(password);
            notification.setProfile(profile);
            notification.setStatus(0);
            notification.setType("success");
            notification.setUser(user);
            notification.setUserCode(userCode);

            notifierClient.creationUser(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #1: " + e.getMessage());
        }

    }

    public void sendNotificationMunicipalityManagementDto(String email, String department, String municipality, Date startDate,
                                                          Long userCode, String supportFile) {

        try {

            MicroserviceNotificationMunicipalityManagementDto notification = new MicroserviceNotificationMunicipalityManagementDto();

            notification.setEmail(email);
            notification.setDpto(department);
            notification.setMpio(municipality);
            notification.setStartDate(startDate);
            notification.setStatus(0);
            notification.setType("success");
            notification.setUserCode(userCode);
            notification.setSupportFile(supportFile);

            notifierClient.municipalityManagement(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #2: " + e.getMessage());
        }

    }

    public void sendNotificationAssignmentOperation(String email, Long userCode, String manager, String municipality,
                                                    String department, Date requestDateFrom, Date requestDateTo, String supportFile) {

        try {

            MicroserviceNotificationAssignmentOperationMunicipalityDto notification = new MicroserviceNotificationAssignmentOperationMunicipalityDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("success");
            notification.setManager(manager);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setRequestNumber("");
            notification.setRequestDateFrom(requestDateFrom);
            notification.setRequestDateTo(requestDateTo);
            notification.setSupportFile(supportFile);

            notifierClient.assignmentOperation(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #3: " + e.getMessage());
        }

    }

    public void sendNotificationInputRequest(String email, Long userCode, String manager, String municipality, String department,
                                             String requestNumber, Date requestDate) {

        try {

            MicroserviceNotificationInputRequestDto notification = new MicroserviceNotificationInputRequestDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("success");
            notification.setManager(manager);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setRequestNumber(requestNumber);
            notification.setRequestDate(requestDate);

            notifierClient.inputRequest(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #4: " + e.getMessage());
        }

    }

    public void sendNotificationLoadOfInputs(String email, Long userCode, boolean loadStatus, String municipality, String department,
                                             String requestNumber, Date loadDate, String supportFile) {

        try {

            MicroserviceNotificationLoadOfInputsDto notification = new MicroserviceNotificationLoadOfInputsDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("success");
            notification.setLoadStatus(loadStatus);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setRequestNumber(requestNumber);
            notification.setLoadDate(loadDate);
            notification.setSupportFile(supportFile);

            notifierClient.loadOfInputs(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #5: " + e.getMessage());
        }

    }

    public void sendNotificationInputIntegrations(String email, Long userCode, String integrationStatus, String municipality,
                                                  String department, Date integrationDate) {

        try {

            MicroserviceNotificationInputIntegrationsDto notification = new MicroserviceNotificationInputIntegrationsDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("success");
            notification.setIntegrationStatus(integrationStatus);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setIntegrationDate(integrationDate);

            notifierClient.inputIntegration(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #6: " + e.getMessage());
        }

    }

    public void sendNotificationTaskAssignment(String email, Long userCode, String task, String municipality, String department,
                                               Date taskDate) {

        try {

            MicroserviceNotificationTaskAssignmentDto notification = new MicroserviceNotificationTaskAssignmentDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("success");
            notification.setTask(task);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setTaskDate(taskDate);

            notifierClient.taskAssignment(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #7: " + e.getMessage());
        }

    }

    public void sendNotificationProductGenerated(String email, Long userCode, String municipality, String department,
                                                 Date requestDate) {

        try {

            MicroserviceNotificationIntegrationFileGenerationDto notification = new MicroserviceNotificationIntegrationFileGenerationDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("success");
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setRequestDate(requestDate);

            notifierClient.productGenerated(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #8: " + e.getMessage());
        }

    }

    public void sendNotificationDeliverySupplies(String email, Long userCode, String manager, String municipality, String department,
                                                 String supportFile, Date requestDate) {

        try {

            MicroserviceNotificationDeliveryOfInputsDto notification = new MicroserviceNotificationDeliveryOfInputsDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("success");
            notification.setManager(manager);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setSupportFile(supportFile);
            notification.setRequestDate(requestDate);

            notifierClient.deliverySupplies(notification);

        } catch (Exception e) {
            log.error("Error enviando la notificación #9: " + e.getMessage());
        }

    }

}
