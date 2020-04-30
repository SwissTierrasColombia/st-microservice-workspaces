package com.ai.st.microservice.workspaces.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.business.ManagerBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceCreateManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceUpdateManagerDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Managers", description = "Manage Managers", tags = { "Managers" })
@RestController
@RequestMapping("api/workspaces/v1/managers")
public class ManagerV1Controller {

	private final Logger log = LoggerFactory.getLogger(ManagerV1Controller.class);

	@Autowired
	private ManagerBusiness managerBusiness;
	
	
	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create manager")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Create manager", response = MicroserviceRequestDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createManager(@RequestBody MicroserviceCreateManagerDto createManagerDto,
			@RequestHeader("authorization") String headerAuthorization) throws DisconnectedMicroserviceException {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			responseDto = managerBusiness.addManager(createManagerDto);
			httpStatus = HttpStatus.CREATED;

		} catch (Exception e) {
			log.error("Error ManagerV1Controller@createManager#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}
	
	
	@RequestMapping(value = "/{managerId}/enable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Activate manager")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Manager enabled", response = MicroserviceManagerDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> activateManager(@PathVariable Long managerId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// get manager
			try {
				managerBusiness.getManagerById(managerId);
			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de gestores.");
			}

			responseDto = managerBusiness.activateManager(managerId);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ManagerV1Controller@activateManager#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (Exception e) {
			log.error("Error ManagerV1Controller@activateManager#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}
	
	@RequestMapping(value = "/{managerId}/disable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Activate manager")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Manager enabled", response = MicroserviceManagerDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> deactivateManager(@PathVariable Long managerId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// get manager
			try {
				managerBusiness.getManagerById(managerId);
			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de gestores.");
			}

			responseDto = managerBusiness.deactivateManager(managerId);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ManagerV1Controller@deactivateManager#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (Exception e) {
			log.error("Error ManagerV1Controller@deactivateManager#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}
	
	@RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Update manager")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Update manager", response = MicroserviceRequestDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> updateManager(@RequestBody MicroserviceUpdateManagerDto updateManagerDto,
			@RequestHeader("authorization") String headerAuthorization) throws DisconnectedMicroserviceException {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			responseDto = managerBusiness.updateManager(updateManagerDto);
			httpStatus = HttpStatus.CREATED;

		} catch (Exception e) {
			log.error("Error ManagerV1Controller@updateManager#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}
	

}
