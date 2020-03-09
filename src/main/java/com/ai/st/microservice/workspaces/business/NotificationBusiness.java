package com.ai.st.microservice.workspaces.business;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private NotifierFeignClient notifierClient;

	public void sendNotificationCreationUser(String email, String password, String profile, String user,
			Long userCode) {

		try {

			MicroserviceNotificationNewUserDto notification = new MicroserviceNotificationNewUserDto();
			notification.setEmail(email);
			notification.setPassword(password);
			notification.setProfile(profile);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setUser(user);
			notification.setUserCode(userCode);

			notifierClient.creationUser(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #1: " + e.getMessage());
		}

	}

	public void sendNotificationMunicipalityManagementDto(String email, String dpto, String mpio, Date startDate,
			Long userCode, String supportFile) {

		try {

			MicroserviceNotificationMunicipalityManagementDto notification = new MicroserviceNotificationMunicipalityManagementDto();

			notification.setEmail(email);
			notification.setDpto(dpto);
			notification.setMpio(mpio);
			notification.setStartDate(startDate);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setUserCode(userCode);
			notification.setSupportFile(supportFile);

			notifierClient.municipalityManagement(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #2: " + e.getMessage());
		}

	}

	public void sendNotificationAssignamentOperation(String email, Long userCode, String manager, String mpio,
			String dpto, Date requestDateFrom, Date requestDateTo, String supportFile) {

		try {

			MicroserviceNotificationAssignmentOperationMunicipalityDto notification = new MicroserviceNotificationAssignmentOperationMunicipalityDto();

			notification.setEmail(email);
			notification.setUserCode(userCode);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setManager(manager);
			notification.setMpio(mpio);
			notification.setDpto(dpto);
			notification.setRequestNumber("");
			notification.setRequestDateFrom(requestDateFrom);
			notification.setRequestDateTo(requestDateTo);
			notification.setSupportFile(supportFile);

			notifierClient.assignmentOperation(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #3: " + e.getMessage());
		}

	}

	public void sendNotificationInputRequest(String email, Long userCode, String manager, String mpio, String dpto,
			String requestNumber, Date requestDate) {

		try {

			MicroserviceNotificationInputRequestDto notification = new MicroserviceNotificationInputRequestDto();

			notification.setEmail(email);
			notification.setUserCode(userCode);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setManager(manager);
			notification.setMpio(mpio);
			notification.setDpto(dpto);
			notification.setRequestNumber(requestNumber);
			notification.setRequestDate(requestDate);

			notifierClient.inputRequest(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #4: " + e.getMessage());
		}

	}

	public void sendNotificationLoadOfInputs(String email, Long userCode, boolean loadStatus, String mpio, String dpto,
			String requestNumber, Date loadDate, String supportFile) {

		try {

			MicroserviceNotificationLoadOfInputsDto notification = new MicroserviceNotificationLoadOfInputsDto();

			notification.setEmail(email);
			notification.setUserCode(userCode);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setLoadStatus(loadStatus);
			notification.setMpio(mpio);
			notification.setDpto(dpto);
			notification.setRequestNumber(requestNumber);
			notification.setLoadDate(loadDate);
			notification.setSupportFile(supportFile);

			notifierClient.loadOfInputs(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #5: " + e.getMessage());
		}

	}

	public void sendNotificationInputIntegrations(String email, Long userCode, String integrationStatus, String mpio,
			String dpto, Date integrationDate) {

		try {

			MicroserviceNotificationInputIntegrationsDto notification = new MicroserviceNotificationInputIntegrationsDto();

			notification.setEmail(email);
			notification.setUserCode(userCode);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setIntegrationStatus(integrationStatus);
			notification.setMpio(mpio);
			notification.setDpto(dpto);
			notification.setIntegrationDate(integrationDate);

			notifierClient.inputIntegration(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #6: " + e.getMessage());
		}

	}

	public void sendNotificationTaskAssignment(String email, Long userCode, String task, String mpio, String dpto,
			Date taskDate) {

		try {

			MicroserviceNotificationTaskAssignmentDto notification = new MicroserviceNotificationTaskAssignmentDto();

			notification.setEmail(email);
			notification.setUserCode(userCode);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setTask(task);
			notification.setMpio(mpio);
			notification.setDpto(dpto);
			notification.setTaskDate(taskDate);

			notifierClient.taskAssignment(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #7: " + e.getMessage());
		}

	}

	public void sendNotificationProductGenerated(String email, Long userCode, String mpio, String dpto,
			Date requestDate) {

		try {

			MicroserviceNotificationIntegrationFileGenerationDto notification = new MicroserviceNotificationIntegrationFileGenerationDto();

			notification.setEmail(email);
			notification.setUserCode(userCode);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setMpio(mpio);
			notification.setDpto(dpto);
			notification.setRequestDate(requestDate);

			notifierClient.productGenerated(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #8: " + e.getMessage());
		}

	}

	public void sendNotificationDeliverySupplies(String email, Long userCode, String manager, String mpio, String dpto,
			String supportFile, Date requestDate) {

		try {

			MicroserviceNotificationDeliveryOfInputsDto notification = new MicroserviceNotificationDeliveryOfInputsDto();

			notification.setEmail(email);
			notification.setUserCode(userCode);
			notification.setStatus(0);
			notification.setType("sucess");
			notification.setManager(manager);
			notification.setMpio(mpio);
			notification.setDpto(dpto);
			notification.setSupportFile(supportFile);
			notification.setRequestDate(requestDate);

			notifierClient.deliverySupplies(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación #9: " + e.getMessage());
		}

	}

}
