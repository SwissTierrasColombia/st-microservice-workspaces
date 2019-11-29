package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleProviderDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceCreateUserDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceAddUserToProviderDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class AdministrationBusiness {

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private ProviderFeignClient providerClient;

	public MicroserviceUserDto createUser(String firstName, String lastName, String email, String username,
			String password, CreateUserRoleProviderDto roleProvider) throws BusinessException {

		MicroserviceUserDto userResponseDto = null;

		MicroserviceCreateUserDto createUserDto = new MicroserviceCreateUserDto();
		createUserDto.setEmail(email);
		createUserDto.setFirstName(firstName);
		createUserDto.setLastName(lastName);
		createUserDto.setPassword(password);
		createUserDto.setUsername(username);

		List<Long> roles = new ArrayList<Long>();
		if (roleProvider != null) {
			if (roleProvider.getRoleId() != null && roleProvider.getRoleId() > 0) {
				roles.add(roleProvider.getRoleId());
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
						throw new BusinessException("No se ha podido asignar el usuario al proveedor de insumo");
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
