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
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.business.WorkspaceBusiness;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.AnswerRequestDto;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.CreateProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.CreateRequestDto;
import com.ai.st.microservice.workspaces.dto.CreateTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.TypeSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceQueryResultRegistralRevisionDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateProviderDto;
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

	@Autowired
	private UserBusiness userBusiness;

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
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserTechnicalOrAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
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

	@RequestMapping(value = "/types-supplies", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create type supply")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Create type supply", response = MicroserviceTypeSupplyDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createTypeSupply(@RequestBody CreateTypeSupplyDto createTypeSupplyDto,
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

			MicroserviceProviderDto providerDto = null;
			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = null;

			try {

				providerDto = providerClient.findProviderByAdministrator(userDtoSession.getId());

				List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
						.findRolesByUser(userDtoSession.getId());

				roleDirector = providerRoles.stream()
						.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores.");
			}

			if (roleDirector == null) {
				throw new BusinessException("No tiene permiso para crear tipos de insumo del proveedor.");
			}

			responseDto = providerBusiness.createTypeSupply(providerDto.getId(), createTypeSupplyDto.getName(),
					createTypeSupplyDto.getDescription(), createTypeSupplyDto.getMetadataRequired(),
					createTypeSupplyDto.getModelRequired(), createTypeSupplyDto.getProviderProfileId(),
					createTypeSupplyDto.getExtensions());
			httpStatus = HttpStatus.CREATED;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@createTypeSupply#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@createTypeSupply#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@createTypeSupply#General ---> " + e.getMessage());
			responseDto = new BasicResponseDto(e.getMessage(), 4);
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/types-supplies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get types supplies")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get types supplies", response = MicroserviceTypeSupplyDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getTypeSupplies(@RequestHeader("authorization") String headerAuthorization) {

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
			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = null;

			try {

				providerDto = providerClient.findProviderByAdministrator(userDtoSession.getId());

				List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
						.findRolesByUser(userDtoSession.getId());

				roleDirector = providerRoles.stream()
						.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores.");
			}

			if (roleDirector == null) {
				throw new BusinessException("No tiene permiso para consultar los tipos de insumo del proveedor.");
			}

			responseDto = providerBusiness.getTypesSuppliesByProvider(providerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getTypeSupplies#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getTypeSupplies#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getTypeSupplies#General ---> " + e.getMessage());
			responseDto = new BasicResponseDto(e.getMessage(), 4);
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/types-supplies/{typeSupplyId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Update type supply")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Update type supply", response = MicroserviceTypeSupplyDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> updateTypeSupply(@PathVariable Long typeSupplyId,
			@RequestBody CreateTypeSupplyDto createTypeSupplyDto,
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
			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = null;

			try {

				providerDto = providerClient.findProviderByAdministrator(userDtoSession.getId());

				List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
						.findRolesByUser(userDtoSession.getId());

				roleDirector = providerRoles.stream()
						.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores.");
			}

			if (roleDirector == null) {
				throw new BusinessException("No tiene permiso para editar tipos de insumo del proveedor.");
			}

			responseDto = providerBusiness.updateTypeSupply(providerDto.getId(), typeSupplyId,
					createTypeSupplyDto.getName(), createTypeSupplyDto.getDescription(),
					createTypeSupplyDto.getMetadataRequired(), createTypeSupplyDto.getModelRequired(),
					createTypeSupplyDto.getProviderProfileId(), createTypeSupplyDto.getExtensions());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@updateTypeSupply#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@updateTypeSupply#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@updateTypeSupply	#General ---> " + e.getMessage());
			responseDto = new BasicResponseDto(e.getMessage(), 4);
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/types-supplies/{typeSupplyId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Delete type supply")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Delete type supply"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> deleteTypeSupply(@PathVariable Long typeSupplyId,
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
			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = null;

			try {

				providerDto = providerClient.findProviderByAdministrator(userDtoSession.getId());

				List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
						.findRolesByUser(userDtoSession.getId());

				roleDirector = providerRoles.stream()
						.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores.");
			}

			if (roleDirector == null) {
				throw new BusinessException("No tiene permiso para eliminar tipos de insumo del proveedor.");
			}

			providerBusiness.deleteTypeSupply(providerDto.getId(), typeSupplyId);
			httpStatus = HttpStatus.NO_CONTENT;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@deleteTypeSupply#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@deleteTypeSupply#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@deleteTypeSupply	#General ---> " + e.getMessage());
			responseDto = new BasicResponseDto(e.getMessage(), 4);
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/profiles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create profile")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Create profile", response = MicroserviceProviderProfileDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createProfile(@RequestBody CreateProviderProfileDto createProfileDto,
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
			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = null;

			try {

				providerDto = providerClient.findProviderByAdministrator(userDtoSession.getId());

				List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
						.findRolesByUser(userDtoSession.getId());

				roleDirector = providerRoles.stream()
						.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores.");
			}

			if (roleDirector == null) {
				throw new BusinessException("No tiene permiso para crear perfiles del proveedor.");
			}

			responseDto = providerBusiness.createProfile(providerDto.getId(), createProfileDto.getName(),
					createProfileDto.getDescription());

			httpStatus = HttpStatus.CREATED;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@createProfile#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@createProfile#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@createProfile#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/profiles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get profiles")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get profiles", response = MicroserviceProviderProfileDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getProfiles(@RequestHeader("authorization") String headerAuthorization) {

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
			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = null;

			try {

				providerDto = providerClient.findProviderByAdministrator(userDtoSession.getId());

				List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
						.findRolesByUser(userDtoSession.getId());

				roleDirector = providerRoles.stream()
						.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores.");
			}

			if (roleDirector == null) {
				throw new BusinessException("No tiene permiso para consultar perfiles del proveedor.");
			}

			responseDto = providerBusiness.getProfilesByProvider(providerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getProfiles#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getProfiles#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getProfiles#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/profiles/{profileId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Update profile")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Update profile", response = MicroserviceProviderProfileDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> updateProfile(@PathVariable Long profileId,
			@RequestHeader("authorization") String headerAuthorization,
			@RequestBody CreateProviderProfileDto updateProfileDto) {

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
			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = null;

			try {

				providerDto = providerClient.findProviderByAdministrator(userDtoSession.getId());

				List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
						.findRolesByUser(userDtoSession.getId());

				roleDirector = providerRoles.stream()
						.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores.");
			}

			if (roleDirector == null) {
				throw new BusinessException("No tiene permiso para editar perfiles del proveedor.");
			}

			responseDto = providerBusiness.updateProfile(providerDto.getId(), profileId, updateProfileDto.getName(),
					updateProfileDto.getDescription());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@updateProfile#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@updateProfile#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@updateProfile#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/profiles/{profileId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Delete profile")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Delete profile"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> deleteProfile(@PathVariable Long profileId,
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
			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = null;

			try {

				providerDto = providerClient.findProviderByAdministrator(userDtoSession.getId());

				List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
						.findRolesByUser(userDtoSession.getId());

				roleDirector = providerRoles.stream()
						.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de proveedores.");
			}

			if (roleDirector == null) {
				throw new BusinessException("No tiene permiso para eliminar perfiles del proveedor.");
			}

			providerBusiness.deleteProfile(providerDto.getId(), profileId);
			httpStatus = HttpStatus.NO_CONTENT;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@deleteProfile#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@deleteProfile#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@deleteProfile#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create provider")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Create provider", response = MicroserviceProviderDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<MicroserviceProviderDto> createProvider(
			@RequestBody MicroserviceCreateProviderDto createProviderDto) {

		HttpStatus httpStatus = null;
		MicroserviceProviderDto responseProviderDto = null;

		try {

			// validation input data
			if (createProviderDto.getName().isEmpty()) {
				throw new InputValidationException("The provider name is required.");
			}
			if (createProviderDto.getTaxIdentificationNumber().isEmpty()) {
				throw new InputValidationException("The tax identification number is required.");
			}
			if (createProviderDto.getProviderCategoryId() == null) {
				throw new InputValidationException("The provider category is required.");
			}

			responseProviderDto = providerBusiness.addProvider(createProviderDto);

			httpStatus = HttpStatus.CREATED;
		} catch (InputValidationException e) {
			log.error("Error ProviderV1Controller@createProvider#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@createProvider#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(responseProviderDto, httpStatus);
	}

	@RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Update provider")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Update provider", response = MicroserviceProviderDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<MicroserviceProviderDto> updateProvider(
			@RequestBody MicroserviceUpdateProviderDto updateProviderDto) {

		HttpStatus httpStatus = null;
		MicroserviceProviderDto responseProviderDto = null;

		try {

			// validation input data
			if (updateProviderDto.getName().isEmpty()) {
				throw new InputValidationException("The provider name is required.");
			}
			if (updateProviderDto.getTaxIdentificationNumber().isEmpty()) {
				throw new InputValidationException("The tax identification number is required.");
			}
			if (updateProviderDto.getProviderCategoryId() == null) {
				throw new InputValidationException("The provider category is required.");
			}

			responseProviderDto = providerBusiness.updateProvider(updateProviderDto);

			httpStatus = HttpStatus.CREATED;
		} catch (InputValidationException e) {
			log.error("Error ProviderV1Controller@updateProvider#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@updateProvider#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(responseProviderDto, httpStatus);
	}

	@RequestMapping(value = "/requests/municipality", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get requests by municipality")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get requests", response = MicroserviceRequestDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getRequestsByMunicipality(@RequestHeader("authorization") String headerAuthorization,
			@RequestParam(name = "page", required = true) Integer page,
			@RequestParam(name = "municipality", required = true) String municipalityCode) {

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

			responseDto = providerBusiness.getRequestsByManagerAndMunicipality(page, managerDto.getId(),
					municipalityCode);

			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getRequestsByMunicipality#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getRequestsByMunicipality#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getRequestsByMunicipality#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/requests/provider", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get requests by municipality")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get requests", response = MicroserviceRequestDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getRequestsByProvider(@RequestHeader("authorization") String headerAuthorization,
			@RequestParam(name = "page", required = true) Integer page,
			@RequestParam(name = "provider", required = true) Long providerId) {

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

			responseDto = providerBusiness.getRequestsByManagerAndProvider(page, managerDto.getId(), providerId);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getRequestsByProvider#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getRequestsByProvider#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getRequestsByProvider#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/requests/package", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get requests by municipality")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get requests", response = MicroserviceRequestDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getRequestsByPackage(@RequestHeader("authorization") String headerAuthorization,
			@RequestParam(name = "package", required = false) String packageLabel) {

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

			responseDto = providerBusiness.getRequestsByManagerAndPackage(managerDto.getId(), packageLabel);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getRequestsByPackage#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getRequestsByPackage#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getRequestsByPackage#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/supplies-review", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get supplies requested to review")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get supplies requested to review", response = MicroserviceSupplyRequestedDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getSuppliesRequestedToReview(
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDelegate(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para consultar insumos pendientes de revisión.");
			}

			responseDto = providerBusiness.getSuppliesToReview(providerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@getSuppliesRequestedToReview#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@getSuppliesRequestedToReview#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@getSuppliesRequestedToReview#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/supplies-review/{supplyRequestedId}/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Start revision")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Start revision", response = BasicResponseDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> startRevision(@RequestHeader("authorization") String headerAuthorization,
			@PathVariable Long supplyRequestedId) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDelegate(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para consultar insumos pendientes de revisión.");
			}

			providerBusiness.startRevision(supplyRequestedId, userDtoSession.getId(), providerDto);
			responseDto = new BasicResponseDto("Revisión iniciada", 7);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@startRevision#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@startRevision#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@startRevision#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/supplies-review/{supplyRequestedId}/records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Start revision")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Start revision", response = MicroserviceQueryResultRegistralRevisionDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getRercodsFromRevision(@RequestHeader("authorization") String headerAuthorization,
			@PathVariable Long supplyRequestedId, @RequestParam(name = "page", required = true) int page) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDelegate(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para consultar esta información.");
			}

			responseDto = providerBusiness.getRecordsFromRevision(providerDto, supplyRequestedId, page);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@startRevision#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@startRevision#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@startRevision#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/supplies-review/{supplyRequestedId}/update/{boundarySpaceId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Start revision")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Start revision", response = MicroserviceQueryResultRegistralRevisionDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> updateRecordBoundarySpace(@RequestHeader("authorization") String headerAuthorization,
			@PathVariable Long supplyRequestedId, @PathVariable Long boundarySpaceId,
			@RequestParam(name = "file", required = true) MultipartFile file) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDelegate(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para consultar esta información.");
			}

			providerBusiness.uploadAttachmentToRevision(providerDto, file, supplyRequestedId, boundarySpaceId,
					userDtoSession.getId());
			responseDto = new BasicResponseDto("Registro actualizado", 7);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@updateRecordBoundarySpace#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@updateRecordBoundarySpace#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@updateRecordBoundarySpace#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/supplies-review/{supplyRequestedId}/close", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Start revision")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Start revision", response = BasicResponseDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> closeRevision(@RequestHeader("authorization") String headerAuthorization,
			@PathVariable Long supplyRequestedId) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDelegate(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para cerrar la revisión.");
			}

			providerBusiness.closeRevision(supplyRequestedId, userDtoSession.getId(), providerDto);
			responseDto = new BasicResponseDto("El proceso de cerrar revisión ha iniciado", 7);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@startRevision#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@startRevision#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@startRevision#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/supplies-review/{supplyRequestedId}/skip", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Start revision")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Start revision", response = BasicResponseDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> skipRevision(@RequestHeader("authorization") String headerAuthorization,
			@PathVariable Long supplyRequestedId) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDelegate(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para omitir la revisión.");
			}

			providerBusiness.skipRevision(supplyRequestedId, userDtoSession.getId(), providerDto);
			responseDto = new BasicResponseDto("Revisión omitida", 7);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error ProviderV1Controller@skipRevision#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error ProviderV1Controller@skipRevision#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error ProviderV1Controller@skipRevision#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

}
