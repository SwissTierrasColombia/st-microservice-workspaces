package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceCreateManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceCreateManagerProfileDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceUpdateManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceUpdateManagerProfileDto;

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

	public MicroserviceManagerDto addManager(MicroserviceCreateManagerDto manager) {
		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.addManager(manager);
		} catch (Exception e) {
			log.error("No se ha podido agregar el gestor: " + e.getMessage());
		}
		return managerDto;
	}

	public MicroserviceManagerDto activateManager(Long managerId) {
		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.activateManager(managerId);
		} catch (Exception e) {
			log.error("No se ha podido activar el gestor: " + e.getMessage());
		}
		return managerDto;
	}

	public MicroserviceManagerDto deactivateManager(Long managerId) {
		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.deactivateManager(managerId);
		} catch (Exception e) {
			log.error("No se ha podido desactivar el gestor: " + e.getMessage());
		}
		return managerDto;
	}
	
	public MicroserviceManagerDto updateManager(MicroserviceUpdateManagerDto manager) {
		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.updateManager(manager);
		} catch (Exception e) {
			log.error("No se ha podido actualizar el gestor: " + e.getMessage());
		}
		return managerDto;
	}
	
	public List<MicroserviceManagerProfileDto> getManagerProfiles() {
		
		List<MicroserviceManagerProfileDto> profiles = new ArrayList<>();

		try {
			profiles = managerClient.getManagerProfiles();
		} catch (Exception e) {
			log.error("No se ha podido consultar los perfiles de gestor: " + e.getMessage());
		}

		return profiles;
	}

	public MicroserviceManagerProfileDto createManagerProfiles(MicroserviceCreateManagerProfileDto manager) {
		MicroserviceManagerProfileDto managerDto = null;
		try {
			managerDto = managerClient.updateManagerProfile(manager);
		} catch (Exception e) {
			log.error("No se ha podido actualizar el gestor: " + e.getMessage());
		}
		return managerDto;
	}

	public MicroserviceManagerProfileDto deleteManagerProfiles(MicroserviceUpdateManagerProfileDto requestUpdateManagerProfile) {
		MicroserviceManagerProfileDto managerDto = null;
		try {
			managerDto = managerClient.deleteManagerProfile(requestUpdateManagerProfile);
		} catch (Exception e) {
			log.error("No se ha podido actualizar el gestor: " + e.getMessage());
		}
		return managerDto;
	}

	public MicroserviceManagerProfileDto updateManagerProfiles(MicroserviceUpdateManagerProfileDto requestUpdateManagerProfile) {
		MicroserviceManagerProfileDto managerDto = null;
		try {
			managerDto = managerClient.updateManagerProfile(requestUpdateManagerProfile);
		} catch (Exception e) {
			log.error("No se ha podido actualizar el gestor: " + e.getMessage());
		}
		return managerDto;
	}

}
