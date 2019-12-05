package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleAdministratorDto;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleManagerDto;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleProviderDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceCreateUserDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceAddUserToManagerDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceAddUserToProviderDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class AdministrationBusiness {

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private ManagerFeignClient managerClient;

	public MicroserviceUserDto createUser(String firstName, String lastName, String email, String username,
			String password, CreateUserRoleProviderDto roleProvider, CreateUserRoleAdministratorDto roleAdmin,
			CreateUserRoleManagerDto roleManager) throws BusinessException {

		MicroserviceUserDto userResponseDto = null;

		MicroserviceCreateUserDto createUserDto = new MicroserviceCreateUserDto();
		createUserDto.setEmail(email);
		createUserDto.setFirstName(firstName);
		createUserDto.setLastName(lastName);
		createUserDto.setPassword(password);
		createUserDto.setUsername(username);

		List<Long> roles = new ArrayList<Long>();

		if (roleProvider != null) {

			if (roleProvider.getProfiles().size() == 0) {
				throw new BusinessException("Para asignar el rol de proveedor se debe especificar al menos un perfil.");
			}

			if (roleProvider.getRoleId() != null && roleProvider.getRoleId() > 0) {
				roles.add(roleProvider.getRoleId());
			}
		}

		if (roleAdmin != null) {
			roles.add(roleAdmin.getRoleId());
		}

		if (roleManager != null) {

			if (roleManager.getProfiles().size() == 0) {
				throw new BusinessException("Para asignar el rol de gestor se debe especificar al menos un perfil.");
			}

			if (roleManager.getRoleId() != null && roleManager.getRoleId() > 0) {
				roles.add(roleManager.getRoleId());
			}
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
						System.out.println("HOLAAA " + e.getMessage());
					}
				}

			} catch (BusinessException e) {
				throw new BusinessException(e.getMessage());
			}

		} else {
			throw new BusinessException("El usuario necesita tener al menos un rol.");
		}

		return userResponseDto;
	}

}
