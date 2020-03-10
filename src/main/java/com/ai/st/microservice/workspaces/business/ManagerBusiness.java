package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerUserDto;

@Component
public class ManagerBusiness {

	private final Logger log = LoggerFactory.getLogger(ManagerBusiness.class);

	@Autowired
	private ManagerFeignClient managerClient;

	public MicroserviceManagerDto getManagerById(Long managerId) {

		MicroserviceManagerDto managerDto = null;

		try {

			managerDto = managerClient.findById(managerId);

		} catch (Exception e) {
			log.error("No se ha podido consultar el gestor: " + e.getMessage());
		}

		return managerDto;
	}

	public List<MicroserviceManagerUserDto> getUserByManager(Long managerId, List<Long> profiles) {

		List<MicroserviceManagerUserDto> users = new ArrayList<>();

		try {
			users = managerClient.findUsersByManager(managerId, profiles);
		} catch (Exception e) {
			log.error("No se ha podido consultar el gestor: " + e.getMessage());
		}

		return users;
	}

}
