package com.ai.st.microservice.workspaces.controllers.v1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.business.ProviderBusiness;
import com.ai.st.microservice.workspaces.business.RoleBusiness;
import com.ai.st.microservice.workspaces.business.WorkspaceBusiness;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.AnswerRequestDto;
import com.ai.st.microservice.workspaces.dto.CreateRequestDto;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.TypeSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;

import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Providers", description = "Manage Providers", tags = { "Providers" })
@RestController
@RequestMapping("api/workspaces/v1/providers")
public class ProviderV1Controller {

	private final Logger log = LoggerFactory.getLogger(ProviderV1Controller.class);

	@Autowired
	private WorkspaceBusiness workspaceBusiness;

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private ManagerFeignClient managerClient;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private ProviderBusiness providerBusiness;

	@RequestMapping(value = "/municipalities/{municipalityId}/requests", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create request")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Create request", response = MicroserviceRequestDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createRequest(@RequestBody CreateRequestDto createRequestDto,
			@RequestHeader("authorization") String headerAuthorization, @PathVariable Long municipalityId) {

		HttpStatus httpStatus = null;
		List<MicroserviceRequestDto> listRequests = new ArrayList<MicroserviceRequestDto>();
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

			// get manager
			MicroserviceManagerDto managerDto = null;
			MicroserviceManagerProfileDto profileDirector = null;
			try {
				managerDto = managerClient.findByUserCode(userDtoSession.getId());

				List<MicroserviceManagerProfileDto> managerProfiles = managerClient
						.findProfilesByUser(userDtoSession.getId());

				profileDirector = managerProfiles.stream()
						.filter(profileDto -> profileDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de gestores.");
			}
			if (profileDirector == null) {
				throw new InputValidationException("Acceso denegado.");
			}

			// validation deadline
			String deadlineString = createRequestDto.getDeadline();
			Date deadline = null;
			if (deadlineString != null && !deadlineString.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					deadline = sdf.parse(deadlineString);
				} catch (Exception e) {
					throw new InputValidationException("La fecha límite es inválida.");
				}
			} else {
				throw new InputValidationException("La fecha límite es requerida.");
			}

			// validation supplies
			List<TypeSupplyRequestedDto> supplies = createRequestDto.getSupplies();
			if (supplies.size() > 0) {
				for (TypeSupplyRequestedDto supplyDto : supplies) {
					if (supplyDto.getTypeSupplyId() == null || supplyDto.getTypeSupplyId() <= 0) {
						throw new InputValidationException("El tipo de insumo es inválido.");
					}
					if (supplyDto.getProviderId() == null || supplyDto.getProviderId() <= 0) {
						throw new InputValidationException("El proveedor de insumo es inválido.");
					}
				}
			} else {
				throw new InputValidationException(
						"La solicitud debe contener al menos un tipo de insumo a solicitar.");
			}

			listRequests = workspaceBusiness.createRequest(deadline, supplies, userDtoSession.getId(),
					managerDto.getId(), municipalityId);
			httpStatus = HttpStatus.CREATED;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@createRequest#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (InputValidationException e) {
			log.error("Error ProviderV1Controller@createRequest#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@createRequest#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@createRequest#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
				: new ResponseEntity<>(listRequests, httpStatus);
	}

	@RequestMapping(value = "/pending-requests", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get pending requests by provider")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get pending requests by provider", response = MicroserviceRequestDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getRequestsPendingByProveedor(
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		List<MicroserviceRequestDto> listRequests = new ArrayList<MicroserviceRequestDto>();
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

			// get provider
			MicroserviceProviderDto providerDto = null;
			try {
				providerDto = providerClient.findByUserCode(userDtoSession.getId());
			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores de insumo.");
			}

			listRequests = workspaceBusiness.getPendingRequestByProvider(userDtoSession.getId(), providerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getRequestsPendingByProveedor#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getRequestsPendingByProveedor#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getRequestsPendingByProveedor#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
				: new ResponseEntity<>(listRequests, httpStatus);
	}

	@RequestMapping(value = "/closed-requests", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get closed-requests")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get closed requests by provider", response = MicroserviceRequestDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getRequestsClosedByProveedor(
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		List<MicroserviceRequestDto> listRequests = new ArrayList<MicroserviceRequestDto>();
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

			// get provider
			MicroserviceProviderDto providerDto = null;
			try {
				providerDto = providerClient.findByUserCode(userDtoSession.getId());
			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores de insumo.");
			}

			listRequests = workspaceBusiness.getClosedRequestByProvider(userDtoSession.getId(), providerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getRequestsClosedByProveedor#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getRequestsClosedByProveedor#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getRequestsClosedByProveedor#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
				: new ResponseEntity<>(listRequests, httpStatus);
	}

	@RequestMapping(value = "/requests/{requestId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Answer request")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Answer request", response = MicroserviceRequestDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> answerRequest(@PathVariable Long requestId,
			@RequestHeader("authorization") String headerAuthorization,
			@RequestParam(name = "files[]", required = false) MultipartFile[] files,
			@ModelAttribute AnswerRequestDto answerRequest) {

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

			// get provider
			MicroserviceProviderDto providerDto = null;
			try {
				providerDto = providerClient.findByUserCode(userDtoSession.getId());
			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores de insumo.");
			}

			// validation type supply
			Long typeSupplyId = answerRequest.getTypeSupplyId();
			if (typeSupplyId == null || typeSupplyId <= 0) {
				throw new InputValidationException("El tipo de insumo es inválido.");
			}

			responseDto = providerBusiness.answerRequest(requestId, typeSupplyId, answerRequest.getJustification(),
					files, answerRequest.getUrl(), providerDto, userDtoSession.getId(),
					answerRequest.getObservations());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@answerRequest#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (InputValidationException e) {
			log.error("Error ProviderV1Controller@answerRequest#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@answerRequest#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@answerRequest#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/requests/{requestId}/close", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Close request")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Close request", response = MicroserviceRequestDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> closeRequest(@PathVariable Long requestId,
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

			// get provider
			MicroserviceProviderDto providerDto = null;
			try {
				providerDto = providerClient.findByUserCode(userDtoSession.getId());
			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores de insumo.");
			}

			responseDto = providerBusiness.closeRequest(requestId, providerDto, userDtoSession.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@answerRequest#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@answerRequest#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@answerRequest#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/requests/emmiters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Close request")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Close request", response = MicroserviceRequestDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getRequestsByEmmiters(@RequestHeader("authorization") String headerAuthorization) {

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

			// get manager
			MicroserviceManagerDto managerDto = null;
			try {
				managerDto = managerClient.findByUserCode(userDtoSession.getId());
			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de gestores.");
			}

			responseDto = providerBusiness.getRequestsByEmmitersManager(managerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getRequestsByEmmiters#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getRequestsByEmmiters#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getRequestsByEmmiters#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

}
