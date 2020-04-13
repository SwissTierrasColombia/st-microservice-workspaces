package com.ai.st.microservice.workspaces.controllers.v1;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.business.AdministrationBusiness;
import com.ai.st.microservice.workspaces.business.RoleBusiness;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.CreateUserDto;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.ChangePasswordDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceRoleDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;

import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Users-Roles", description = "Manage Users-Roles", tags = { "Administration" })
@RestController
@RequestMapping("api/workspaces/v1/administration")
public class AdministrationV1Controller {

	private final Logger log = LoggerFactory.getLogger(AdministrationV1Controller.class);

	@Autowired
	private AdministrationBusiness administrationBusiness;

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private ManagerFeignClient managerClient;

	@RequestMapping(value = "/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create user")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Create user", response = MicroserviceUserDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createUser(@RequestBody CreateUserDto requestCreateUser,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			String token = headerAuthorization.replace("Bearer ", "").trim();
			MicroserviceUserDto userDtoSession = null;
			try {
				userDtoSession = userClient.findByToken(token);
			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de usuarios.");
			}

			MicroserviceRoleDto roleSuper = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId().equals(RoleBusiness.ROLE_SUPER_ADMINISTRATOR)).findAny()
					.orElse(null);

			MicroserviceRoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId().equals(RoleBusiness.ROLE_ADMINISTRATOR)).findAny().orElse(null);

			MicroserviceRoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId().equals(RoleBusiness.ROLE_MANAGER)).findAny().orElse(null);

			if (roleSuper instanceof MicroserviceRoleDto) {
				responseDto = administrationBusiness.createUser(requestCreateUser.getFirstName(),
						requestCreateUser.getLastName(), requestCreateUser.getEmail(), requestCreateUser.getUsername(),
						requestCreateUser.getPassword(), null, requestCreateUser.getRoleAdministrator(), null, null);
			}

			if (roleAdministrator instanceof MicroserviceRoleDto) {
				requestCreateUser.getRoleManager().setIsManager(false);
				responseDto = administrationBusiness.createUser(requestCreateUser.getFirstName(),
						requestCreateUser.getLastName(), requestCreateUser.getEmail(), requestCreateUser.getUsername(),
						requestCreateUser.getPassword(), requestCreateUser.getRoleProvider(),
						requestCreateUser.getRoleAdministrator(), requestCreateUser.getRoleManager(),
						requestCreateUser.getRoleOperator());
			}

			if (roleManager instanceof MicroserviceRoleDto) {

				MicroserviceManagerProfileDto profileDirector = null;
				try {

					List<MicroserviceManagerProfileDto> managerProfiles = managerClient
							.findProfilesByUser(userDtoSession.getId());

					profileDirector = managerProfiles.stream()
							.filter(profileDto -> profileDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR)).findAny()
							.orElse(null);

				} catch (Exception e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				requestCreateUser.getRoleManager().setIsManager(true);
				requestCreateUser.getRoleManager().setIsDirector(profileDirector == null ? false : true);

				responseDto = administrationBusiness.createUser(requestCreateUser.getFirstName(),
						requestCreateUser.getLastName(), requestCreateUser.getEmail(), requestCreateUser.getUsername(),
						requestCreateUser.getPassword(), null, null, requestCreateUser.getRoleManager(), null);
			}

			httpStatus = HttpStatus.CREATED;

		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/users/reset-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create user")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Create user", response = MicroserviceUserDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> resetUserPassword(@RequestBody ChangePasswordDto requestChangePassword,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			String token = headerAuthorization.replace("Bearer ", "").trim();
			MicroserviceUserDto userDtoSession = null;
			try {
				userDtoSession = userClient.findByToken(token);
			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de usuarios.");
			}

			responseDto = administrationBusiness.changeUserPassword(userDtoSession.getId(),
					requestChangePassword.getPassword());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error AdministrationV1Controller@resetUserPassword#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error AdministrationV1Controller@resetUserPassword#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error AdministrationV1Controller@resetUserPassword#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

}
