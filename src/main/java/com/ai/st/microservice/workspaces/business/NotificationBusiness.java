package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.NotifierFeignClient;
import com.ai.st.microservice.common.dto.notifier.*;

import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

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
            notification.setType("creationUser");
            notification.setUser(user);
            notification.setUserCode(userCode);

            notifierClient.creationUser(notification);

        } catch (Exception e) {
            String message = String.format("Error enviando la notificación al crear usuario: %s", e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationCreationSinicUser(String email, String password, String profile, String user,
            Long userCode) {

        try {

            MicroserviceNotificationNewUserDto notification = new MicroserviceNotificationNewUserDto();
            notification.setEmail(email);
            notification.setPassword(password);
            notification.setProfile(profile);
            notification.setStatus(0);
            notification.setType("creationSinicUser");
            notification.setUser(user);
            notification.setUserCode(userCode);

            notifierClient.creationUser(notification);

        } catch (Exception e) {
            String message = String.format("Error enviando la notificación al crear usuario sinic: %s", e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationMunicipalityManagementDto(String email, String department, String municipality,
            Date startDate, Long userCode, String supportFile) {

        try {

            MicroserviceNotificationMunicipalityManagementDto notification = new MicroserviceNotificationMunicipalityManagementDto();

            notification.setEmail(email);
            notification.setDpto(department);
            notification.setMpio(municipality);
            notification.setStartDate(startDate);
            notification.setStatus(0);
            notification.setType("assignManager");
            notification.setUserCode(userCode);
            notification.setSupportFile(supportFile);

            notifierClient.municipalityManagement(notification);

        } catch (Exception e) {
            String message = String.format("Error enviando la notificación al asignar un municipio al gestor: %s",
                    e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationAssignmentOperation(String email, Long userCode, String manager, String municipality,
            String department, Date requestDateFrom, Date requestDateTo, String supportFile) {

        try {

            MicroserviceNotificationAssignmentOperationMunicipalityDto notification = new MicroserviceNotificationAssignmentOperationMunicipalityDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("assignOperator");
            notification.setManager(manager);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setRequestNumber("");
            notification.setRequestDateFrom(requestDateFrom);
            notification.setRequestDateTo(requestDateTo);
            notification.setSupportFile(supportFile);

            notifierClient.assignmentOperation(notification);

        } catch (Exception e) {
            String message = String.format("Error enviando la notificación al asignar un operador al municipio: %s",
                    e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationInputRequest(String email, Long userCode, String manager, String municipality,
            String department, String requestNumber, Date requestDate) {

        try {

            MicroserviceNotificationInputRequestDto notification = new MicroserviceNotificationInputRequestDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("createSuppliesRequest");
            notification.setManager(manager);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setRequestNumber(requestNumber);
            notification.setRequestDate(requestDate);

            notifierClient.inputRequest(notification);

        } catch (Exception e) {
            String message = String.format("Error enviando la notificación al crear una solicitud de insumos: %s",
                    e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationLoadOfInputs(String email, Long userCode, boolean loadStatus, String municipality,
            String department, String requestNumber, Date loadDate, String supportFile) {

        try {

            MicroserviceNotificationLoadOfInputsDto notification = new MicroserviceNotificationLoadOfInputsDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("loadXTFFileInSuppliesModule");
            notification.setLoadStatus(loadStatus);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setRequestNumber(requestNumber);
            notification.setLoadDate(loadDate);
            notification.setSupportFile(supportFile);

            notifierClient.loadOfInputs(notification);

        } catch (Exception e) {
            String message = String.format(
                    "Error enviando la notificación al cargar un archivo XTF en el módulo de insumos: %s",
                    e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationInputIntegrations(String email, Long userCode, String integrationStatus,
            String municipality, String department, Date integrationDate) {

        try {

            MicroserviceNotificationInputIntegrationsDto notification = new MicroserviceNotificationInputIntegrationsDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("resultIntegration");
            notification.setIntegrationStatus(integrationStatus);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setIntegrationDate(integrationDate);

            notifierClient.inputIntegration(notification);

        } catch (Exception e) {
            String message = String.format(
                    "Error enviando la notificación al finalizar una integración catastro-registro: %s",
                    e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationTaskAssignment(String email, Long userCode, String task, String municipality,
            String department, Date taskDate) {

        try {

            MicroserviceNotificationTaskAssignmentDto notification = new MicroserviceNotificationTaskAssignmentDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("taskAssignment");
            notification.setTask(task);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setTaskDate(taskDate);

            notifierClient.taskAssignment(notification);

        } catch (Exception e) {
            String message = String.format("Error enviando la notificación al crear una tarea: %s", e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationProductGenerated(String email, Long userCode, String municipality, String department,
            Date requestDate) {

        try {

            MicroserviceNotificationIntegrationFileGenerationDto notification = new MicroserviceNotificationIntegrationFileGenerationDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("productGeneratedFromIntegration");
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setRequestDate(requestDate);

            notifierClient.productGenerated(notification);

        } catch (Exception e) {
            String message = String.format("Error enviando la notificación al generarse un producto: %s",
                    e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

    public void sendNotificationDeliverySupplies(String email, Long userCode, String manager, String municipality,
            String department, String supportFile, Date requestDate) {

        try {

            MicroserviceNotificationDeliveryOfInputsDto notification = new MicroserviceNotificationDeliveryOfInputsDto();

            notification.setEmail(email);
            notification.setUserCode(userCode);
            notification.setStatus(0);
            notification.setType("deliveryCreatedForOperator");
            notification.setManager(manager);
            notification.setMpio(municipality);
            notification.setDpto(department);
            notification.setSupportFile(supportFile);
            notification.setRequestDate(requestDate);

            notifierClient.deliverySupplies(notification);

        } catch (Exception e) {
            String message = String.format("Error enviando la notificación al crear entrega: %s", e.getMessage());
            SCMTracing.sendError(message);
            log.error(message);
        }

    }

}
