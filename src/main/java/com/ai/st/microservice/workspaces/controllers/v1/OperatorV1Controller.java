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

import com.ai.st.microservice.workspaces.business.OperatorBusiness;
import com.ai.st.microservice.workspaces.business.WorkspaceOperatorBusiness;
import com.ai.st.microservice.workspaces.clients.OperatorFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceUpdateOperatorDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;

import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Operators", description = "Manage Operators", tags = { "Operators" })
@RestController
@RequestMapping("api/workspaces/v1/operators")
public class OperatorV1Controller {

	private final Logger log = LoggerFactory.getLogger(OperatorV1Controller.class);

	@Autowired
	private WorkspaceOperatorBusiness workspaceOperatorBusiness;

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private OperatorFeignClient operatorClient;
	
	@Autowired
	private OperatorBusiness operatorBusiness;

	@RequestMapping(value = "/deliveries/{deliveryId}/disable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Disable delivery")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Delivery disabled", response = MicroserviceDeliveryDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> disableDelivery(@PathVariable Long deliveryId,
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

			// get operator
			MicroserviceOperatorDto operatorDto = null;
			try {
				operatorDto = operatorClient.findByUserCode(userDtoSession.getId());
			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de operadores.");
			}

			responseDto = workspaceOperatorBusiness.disableDelivery(operatorDto.getId(), deliveryId);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error OperatorV1Controller@disableDelivery#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error OperatorV1Controller@disableDelivery#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error OperatorV1Controller@disableDelivery#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}
	
	
	
	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create operator")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Create operator", response = MicroserviceOperatorDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createOperator(@RequestBody MicroserviceCreateOperatorDto createOperatorDto,
			@RequestHeader("authorization") String headerAuthorization) throws DisconnectedMicroserviceException {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			responseDto = operatorBusiness.addOperator(createOperatorDto);
			httpStatus = HttpStatus.CREATED;

		} catch (Exception e) {
			log.error("Error OperatorV1Controller@createOperator#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}
	
	
	@RequestMapping(value = "/{operatorId}/enable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Activate operator")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Operator enabled", response = MicroserviceOperatorDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> activateOperator(@PathVariable Long operatorId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// get manager
			try {
				operatorBusiness.getOperatorById(operatorId);
			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de operadores.");
			}

			responseDto = operatorBusiness.activateOperator(operatorId);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error OperatorV1Controller@activateOperator#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (Exception e) {
			log.error("Error OperatorV1Controller@activateOperator#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}
	
	@RequestMapping(value = "/{operatorId}/disable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Disable operator")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Operator disabled", response = MicroserviceManagerDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> deactivateOperator(@PathVariable Long operatorId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// get manager
			try {
				operatorBusiness.getOperatorById(operatorId);
			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de operadores.");
			}

			responseDto = operatorBusiness.deactivateOperator(operatorId);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error OperatorV1Controller@deactivateOperator#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (Exception e) {
			log.error("Error OperatorV1Controller@deactivateOperator#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}
	
	@RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Update operator")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Update operator", response = MicroserviceRequestDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> updateOperator(@RequestBody MicroserviceUpdateOperatorDto updateOperatorDto,
			@RequestHeader("authorization") String headerAuthorization) throws DisconnectedMicroserviceException {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			responseDto = operatorBusiness.updateOperator(updateOperatorDto);
			httpStatus = HttpStatus.CREATED;

		} catch (Exception e) {
			log.error("Error OperatorV1Controller@updateOperator#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

}
