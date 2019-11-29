package com.ai.st.microservice.workspaces.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.business.AdministrationBusiness;
import com.ai.st.microservice.workspaces.dto.CreateUserDto;
import com.ai.st.microservice.workspaces.dto.ErrorDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;

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

	@RequestMapping(value = "/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create user")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Create user", response = MicroserviceUserDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createUser(@RequestBody CreateUserDto requestCreateUser) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// validation roles
			if (requestCreateUser.getRoleProvider() == null) {
				throw new InputValidationException("El usuario tiene que tener al menos un rol.");
			}

			responseDto = administrationBusiness.createUser(requestCreateUser.getFirstName(),
					requestCreateUser.getLastName(), requestCreateUser.getEmail(), requestCreateUser.getUsername(),
					requestCreateUser.getPassword(), requestCreateUser.getRoleProvider());
			httpStatus = HttpStatus.CREATED;

		} catch (InputValidationException e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new ErrorDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new ErrorDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new ErrorDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

}
