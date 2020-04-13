package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleAdministratorDto;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleManagerDto;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleOperatorDto;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleProviderDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceChangePasswordDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceCreateUserDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceAddUserToManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceAddUserToProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class AdministrationBusiness {

	private final Logger log = LoggerFactory.getLogger(AdministrationBusiness.class);

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private ManagerFeignClient managerClient;

	@Autowired
	private NotificationBusiness notificationBusiness;

	@Autowired
	private ProviderBusiness providerBusiness;

	@Autowired
	private ManagerBusiness managerBusiness;

	@Autowired
	private OperatorBusiness operatorBusiness;

	public MicroserviceUserDto createUser(String firstName, String lastName, String email, String username,
			String password, CreateUserRoleProviderDto roleProvider, CreateUserRoleAdministratorDto roleAdmin,
			CreateUserRoleManagerDto roleManager, CreateUserRoleOperatorDto roleOperator) throws BusinessException {

		MicroserviceUserDto userResponseDto = null;

		MicroserviceCreateUserDto createUserDto = new MicroserviceCreateUserDto();
		createUserDto.setEmail(email);
		createUserDto.setFirstName(firstName);
		createUserDto.setLastName(lastName);
		createUserDto.setPassword(password);
		createUserDto.setUsername(username);

		List<Long> roles = new ArrayList<Long>();

		String entityName = "";

		if (roleProvider != null) {

			if (roleProvider.getProfiles().size() == 0) {
				throw new BusinessException("Para asignar el rol de proveedor se debe especificar al menos un perfil.");
			}

			if (roleProvider.getRoleId() != null && roleProvider.getRoleId() > 0) {
				roles.add(roleProvider.getRoleId());
			}

			MicroserviceProviderDto providerDto = providerBusiness.getProviderById(roleProvider.getProviderId());
			entityName = (providerDto != null) ? providerDto.getName() : "";
		}

		if (roleAdmin != null) {
			roles.add(roleAdmin.getRoleId());
			entityName = "ADMINISTRADOR";
		}

		if (roleManager != null) {

			if (roleManager.getProfiles().size() == 0) {
				throw new BusinessException("Para asignar el rol de gestor se debe especificar al menos un perfil.");
			}

			if (roleManager.getRoleId() != null && roleManager.getRoleId() > 0) {

				if (roleManager.getIsManager()) {

					if (roleManager.getIsDirector()) {
						roles.add(roleManager.getRoleId());
					} else {
						throw new BusinessException("No se cuenta con los permisos necesarios para asociar usuarios al gestor.");
					}
					
				} else {
					roles.add(roleManager.getRoleId());
				}

			}

			MicroserviceManagerDto managerDto = managerBusiness.getManagerById(roleManager.getManagerId());
			entityName = (managerDto != null) ? managerDto.getName() : "";
		}

		if (roleOperator != null) {

			if (roleOperator.getRoleId() != null && roleOperator.getRoleId() > 0) {
				roles.add(roleOperator.getRoleId());
			}

			MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorById(roleOperator.getOperatorId());
			entityName = (operatorDto != null) ? operatorDto.getName() : "";
		}

		createUserDto.setRoles(roles);

		if (roles.size() > 0) {

			try {
				userResponseDto = userClient.createUser(createUserDto);

				if (roleProvider != null) {
					try {
						for (Long profileId : roleProvider.getProfiles()) {
							MicroserviceAddUserToProviderDto addUser = new MicroserviceAddUserToProviderDto();
							addUser.setUserCode(userResponseDto.getId());
							addUser.setProfileId(profileId);
							addUser.setProviderId(roleProvider.getProviderId());

							providerClient.addUserToProvide(addUser);
						}
					} catch (Exception e) {
						log.error("Error adding profile to provider: " + e.getMessage());
					}
				}

				if (roleManager != null) {
					try {

						for (Long profileId : roleManager.getProfiles()) {

							MicroserviceAddUserToManagerDto addUser = new MicroserviceAddUserToManagerDto();
							addUser.setUserCode(userResponseDto.getId());
							addUser.setProfileId(profileId);
							addUser.setManagerId(roleManager.getManagerId());

							managerClient.addUserToManager(addUser);
						}

					} catch (Exception e) {
						log.error("Error adding profile to manager: " + e.getMessage());
					}
				}

				if (roleOperator != null) {
					operatorBusiness.addUserToOperator(roleOperator.getOperatorId(), userResponseDto.getId());
				}

			} catch (BusinessException e) {
				throw new BusinessException(e.getMessage());
			}

		} else {
			throw new BusinessException("El usuario necesita tener al menos un rol.");
		}

		// send notification
		try {
			notificationBusiness.sendNotificationCreationUser(email, password, entityName, username,
					userResponseDto.getId());
		} catch (Exception e) {

		}

		return userResponseDto;
	}

	public MicroserviceUserDto changeUserPassword(Long userId, String password) throws BusinessException {

		MicroserviceUserDto userDto = null;

		try {

			MicroserviceChangePasswordDto requestChangePassword = new MicroserviceChangePasswordDto();
			requestChangePassword.setPassword(password);

			userDto = userClient.changeUserPassword(userId, requestChangePassword);

		} catch (BusinessException e) {
			throw new BusinessException(e.getMessage());
		}

		return userDto;

	}

}
