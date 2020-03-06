package com.ai.st.microservice.workspaces.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.NotifierFeignClient;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationNewUserDto;

@Component
public class NotificationBusiness {

	private final Logger log = LoggerFactory.getLogger(NotificationBusiness.class);

	@Autowired
	private NotifierFeignClient notifierClient;

	public void sendNotificationCreationUser(String email, String password, String profile, int status, String type,
			String user, Long userCode) {

		try {

			MicroserviceNotificationNewUserDto notification = new MicroserviceNotificationNewUserDto();
			notification.setEmail(email);
			notification.setPassword(password);
			notification.setProfile(profile);
			notification.setStatus(status);
			notification.setType(type);
			notification.setUser(user);
			notification.setUserCode(userCode);

			notifierClient.creationUser(notification);

		} catch (Exception e) {
			log.error("Error enviando la notificación: " + e.getMessage());
		}

	}

}
