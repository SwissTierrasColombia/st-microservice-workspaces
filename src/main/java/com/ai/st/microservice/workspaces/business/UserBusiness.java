package com.ai.st.microservice.workspaces.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;

@Component
public class UserBusiness {

	private final Logger log = LoggerFactory.getLogger(UserBusiness.class);

	@Autowired
	private UserFeignClient userClient;

	public MicroserviceUserDto getUserById(Long userId) {

		MicroserviceUserDto userDto = null;

		try {

			userDto = userClient.findById(userId);

		} catch (Exception e) {
			log.info("Error consultando el usuario: " + e.getMessage());
		}

		return userDto;
	}

	public MicroserviceUserDto getUserByToken(String headerAuthorization) {

		MicroserviceUserDto userDto = null;

		try {

			String token = headerAuthorization.replace("Bearer ", "").trim();
			userDto = userClient.findByToken(token);

		} catch (Exception e) {
			log.info("Error consultando el usuario: " + e.getMessage());
		}

		return userDto;
	}

}
