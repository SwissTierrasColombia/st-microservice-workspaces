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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.RoleBusiness;
import com.ai.st.microservice.workspaces.business.WorkspaceBusiness;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.AssignOperatorWorkpaceDto;
import com.ai.st.microservice.workspaces.dto.CreateWorkspaceDto;
import com.ai.st.microservice.workspaces.dto.IntegrationDto;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.MakeIntegrationDto;
import com.ai.st.microservice.workspaces.dto.SupportDto;
import com.ai.st.microservice.workspaces.dto.UpdateWorkpaceDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceOperatorDto;
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

@Api(value = "Manage Workspaces", description = "Manage Workspaces", tags = { "Workspaces" })
@RestController
@RequestMapping("api/workspaces/v1/workspaces")
public class WorkspaceV1Controller {

	private final Logger log = LoggerFactory.getLogger(WorkspaceV1Controller.class);

	@Autowired
	private ManagerFeignClient managerClient;

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private WorkspaceBusiness workspaceBusiness;

	@Autowired
	private IntegrationBusiness integrationBusiness;

	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create workspace")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Create Workspace", response = WorkspaceDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createWorkspace(@ModelAttribute CreateWorkspaceDto requestCreateWorkspace) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// validation manager code
			Long managerCode = requestCreateWorkspace.getManagerCode();
			if (managerCode == null || managerCode <= 0) {
				throw new InputValidationException("El gestor es requerido.");
			}

			// validation municipality
			Long municipalityId = requestCreateWorkspace.getMunicipalityId();
			if (municipalityId == null || municipalityId <= 0) {
				throw new InputValidationException("El municipio es requerido.");
			}

			// validation observations
			String observations = requestCreateWorkspace.getObservations();
			if (observations == null || observations.isEmpty()) {
				throw new InputValidationException("Las observaciones son requeridas.");
			}

			// validation start date
			String startDateString = requestCreateWorkspace.getStartDate();
			Date startDate = null;
			if (startDateString != null && !startDateString.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					startDate = sdf.parse(startDateString);
				} catch (Exception e) {
					throw new InputValidationException("La fecha de inicio es inválida.");
				}
			} else {
				throw new InputValidationException("La fecha de inicio es requerida.");
			}

			// validation support
			MultipartFile supporFile = requestCreateWorkspace.getSupportFile();
			if (supporFile.isEmpty()) {
				throw new InputValidationException("El archivo de soporte es requerido.");
			}

			// validation number alphanumeric parcels
			Long parcelsNumber = requestCreateWorkspace.getNumberAlphanumericParcels();
			if (parcelsNumber != null) {
				if (parcelsNumber < 0) {
					throw new InputValidationException("El número de predios alfanuméricos es inválido.");
				}
			}

			// validation municipality area
			Double municipalityArea = requestCreateWorkspace.getMunicipalityArea();
			if (parcelsNumber != null) {
				if (parcelsNumber < 0) {
					throw new InputValidationException("El área del municipio es inválida.");
				}
			}

			responseDto = workspaceBusiness.createWorkspace(startDate, managerCode, municipalityId, observations,
					parcelsNumber, municipalityArea, requestCreateWorkspace.getSupportFile());
			httpStatus = HttpStatus.CREATED;
		} catch (InputValidationException e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
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

	@RequestMapping(value = "/municipalities/{municipalityId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get workspaces by municipality")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get workspaces by municipality", response = WorkspaceDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<List<WorkspaceDto>> getWorkspacesByMunicipality(@PathVariable Long municipalityId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		List<WorkspaceDto> listWorkspaces = new ArrayList<WorkspaceDto>();

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

			MicroserviceRoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_ADMINISTRATOR).findAny().orElse(null);

			MicroserviceRoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_MANAGER).findAny().orElse(null);

			if (roleAdministrator instanceof MicroserviceRoleDto) {

				listWorkspaces = workspaceBusiness.getWorkspacesByMunicipality(municipalityId, null);

			} else if (roleManager instanceof MicroserviceRoleDto) {

				// verify that you have access to the municipality

				// get manager
				MicroserviceManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				listWorkspaces = workspaceBusiness.getWorkspacesByMunicipality(municipalityId, managerDto.getId());
			}

			httpStatus = HttpStatus.CREATED;
		} catch (DisconnectedMicroserviceException e) {
			listWorkspaces = null;
			log.error("Error WorkspaceV1Controller@getWorkspacesByMunicipality#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		} catch (BusinessException e) {
			listWorkspaces = null;
			log.error("Error WorkspaceV1Controller@getWorkspacesByMunicipality#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
		} catch (Exception e) {
			listWorkspaces = null;
			log.error("Error WorkspaceV1Controller@getWorkspacesByMunicipality#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(listWorkspaces, httpStatus);
	}

	@RequestMapping(value = "/{workspaceId}/operators", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Assign operator to workspace (municipality)")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Assign operator to workspace", response = WorkspaceDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> assignOperator(@PathVariable Long workspaceId,
			@ModelAttribute AssignOperatorWorkpaceDto requestAssignOperator,
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

			// get manager
			MicroserviceManagerDto managerDto = null;
			MicroserviceManagerProfileDto profileDirector = null;
			try {
				managerDto = managerClient.findByUserCode(userDtoSession.getId());

				List<MicroserviceManagerProfileDto> managerProfiles = managerClient
						.findProfilesByUser(userDtoSession.getId());

				profileDirector = managerProfiles.stream()
						.filter(profileDto -> profileDto.getId() == RoleBusiness.SUB_ROLE_DIRECTOR).findAny()
						.orElse(null);

			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de gestores.");
			}
			if (profileDirector == null) {
				throw new InputValidationException("Acceso denegado.");
			}

			// validation start date
			String startDateString = requestAssignOperator.getStartDate();
			Date startDate = null;
			if (startDateString != null && !startDateString.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					startDate = sdf.parse(startDateString);
				} catch (Exception e) {
					throw new InputValidationException("La fecha de inicio es inválida.");
				}
			} else {
				throw new InputValidationException("La fecha de inicio es requerida.");
			}

			// validation end date
			String endDateString = requestAssignOperator.getEndDate();
			Date endDate = null;
			if (endDateString != null && !endDateString.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					endDate = sdf.parse(endDateString);
				} catch (Exception e) {
					throw new InputValidationException("La fecha de finalización es inválida.");
				}
			} else {
				throw new InputValidationException("La fecha de finalización es requerida.");
			}

			// validation number parcels expected
			Long parcelsNumber = requestAssignOperator.getNumberParcelsExpected();
			if (parcelsNumber != null) {
				if (parcelsNumber < 0) {
					throw new InputValidationException("El número de predios es inválido.");
				}
			}

			// validation operator code
			Long operatorCode = requestAssignOperator.getOperatorCode();
			if (operatorCode == null || operatorCode <= 0) {
				throw new InputValidationException("El operador es requerido.");
			}

			// validation municipality area
			Double workArea = requestAssignOperator.getWorkArea();
			if (workArea != null) {
				if (workArea < 0) {
					throw new InputValidationException("El área es inválida.");
				}
			}

			// validation support
			MultipartFile supportFile = requestAssignOperator.getSupportFile();
			if (supportFile.isEmpty()) {
				throw new InputValidationException("El archivo de soporte es requerido.");
			}

			responseDto = workspaceBusiness.assignOperator(workspaceId, startDate, endDate, operatorCode, parcelsNumber,
					workArea, supportFile, requestAssignOperator.getObservations(), managerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error WorkspaceV1Controller@assignOperator#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (InputValidationException e) {
			log.error("Error WorkspaceV1Controller@assignOperator#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@assignOperator#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@assignOperator#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/{workspaceId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Update workspace")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Update Workspace", response = WorkspaceDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> updateWorkspace(@ModelAttribute UpdateWorkpaceDto requestUpdateWorkspace,
			@PathVariable Long workspaceId, @RequestHeader("authorization") String headerAuthorization) {

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
			MicroserviceManagerProfileDto profileDirector = null;
			try {
				managerDto = managerClient.findByUserCode(userDtoSession.getId());

				List<MicroserviceManagerProfileDto> managerProfiles = managerClient
						.findProfilesByUser(userDtoSession.getId());

				profileDirector = managerProfiles.stream()
						.filter(profileDto -> profileDto.getId() == RoleBusiness.SUB_ROLE_DIRECTOR).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de gestores.");
			}
			if (profileDirector == null) {
				throw new InputValidationException("Acceso denegado.");
			}

			// validation observations
			String observations = requestUpdateWorkspace.getObservations();
			if (observations == null || observations.isEmpty()) {
				throw new InputValidationException("Las observaciones son requeridas.");
			}

			// validation start date
			String startDateString = requestUpdateWorkspace.getStartDate();
			Date startDate = null;
			if (startDateString != null && !startDateString.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					startDate = sdf.parse(startDateString);
				} catch (Exception e) {
					throw new InputValidationException("La fecha de inicio es inválida.");
				}
			} else {
				throw new InputValidationException("La fecha de inicio es requerida.");
			}

			// validation number alphanumeric parcels
			Long parcelsNumber = requestUpdateWorkspace.getNumberAlphanumericParcels();
			if (parcelsNumber != null) {
				if (parcelsNumber < 0) {
					throw new InputValidationException("El número de predios alfanuméricos es inválido.");
				}
			}

			// validation municipality area
			Double municipalityArea = requestUpdateWorkspace.getMunicipalityArea();
			if (parcelsNumber != null) {
				if (parcelsNumber < 0) {
					throw new InputValidationException("El área del municipio es inválida.");
				}
			}

			responseDto = workspaceBusiness.updateWorkspace(workspaceId, startDate, observations, parcelsNumber,
					municipalityArea, managerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error WorkspaceV1Controller@updateWorkspace#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (InputValidationException e) {
			log.error("Error WorkspaceV1Controller@updateWorkspace#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 1);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@updateWorkspace#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@updateWorkspace#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/{workspaceId}/supports", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get supports by workspace")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get supports by workspace", response = WorkspaceDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getSupportsByWorkspace(@ModelAttribute UpdateWorkpaceDto requestUpdateWorkspace,
			@PathVariable Long workspaceId, @RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		List<SupportDto> listSupports = new ArrayList<SupportDto>();
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

			MicroserviceRoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_ADMINISTRATOR).findAny().orElse(null);

			MicroserviceRoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_MANAGER).findAny().orElse(null);

			if (roleAdministrator instanceof MicroserviceRoleDto) {

				listSupports = workspaceBusiness.getSupportsByWorkspaceId(workspaceId, null);

			} else if (roleManager instanceof MicroserviceRoleDto) {

				// get manager
				MicroserviceManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				listSupports = workspaceBusiness.getSupportsByWorkspaceId(workspaceId, managerDto.getId());
			}

			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error WorkspaceV1Controller@getSupportsByWorkspace#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@getSupportsByWorkspace#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@getSupportsByWorkspace#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
				: new ResponseEntity<>(listSupports, httpStatus);
	}

	@RequestMapping(value = "/{workspaceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get workspace by id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Get workspace by id", response = WorkspaceDto.class),
			@ApiResponse(code = 404, message = "Workspace not found", response = String.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getWorkspaceById(@PathVariable Long workspaceId,
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

			MicroserviceRoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_ADMINISTRATOR).findAny().orElse(null);

			MicroserviceRoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_MANAGER).findAny().orElse(null);

			if (roleAdministrator instanceof MicroserviceRoleDto) {
				responseDto = workspaceBusiness.getWorkspaceById(workspaceId, null);
			} else if (roleManager instanceof MicroserviceRoleDto) {

				// get manager
				MicroserviceManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				responseDto = workspaceBusiness.getWorkspaceById(workspaceId, managerDto.getId());
			}

			httpStatus = (responseDto instanceof WorkspaceDto) ? HttpStatus.OK : HttpStatus.NOT_FOUND;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error WorkspaceV1Controller@getWorkspaceById#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@getWorkspaceById#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@getWorkspaceById#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/{workspaceId}/operators", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get operators by workspace")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get operators by workspace", response = WorkspaceDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getOperatorsByWorkspace(@PathVariable Long workspaceId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		List<WorkspaceOperatorDto> listOperators = new ArrayList<WorkspaceOperatorDto>();
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

			MicroserviceRoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_ADMINISTRATOR).findAny().orElse(null);

			MicroserviceRoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_MANAGER).findAny().orElse(null);

			if (roleAdministrator instanceof MicroserviceRoleDto) {
				listOperators = workspaceBusiness.getOperatorsByWorkspaceId(workspaceId, null);
			} else if (roleManager instanceof MicroserviceRoleDto) {

				// get manager
				MicroserviceManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				listOperators = workspaceBusiness.getOperatorsByWorkspaceId(workspaceId, managerDto.getId());
			}

			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error WorkspaceV1Controller@getOperatorsByWorkspace#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@getOperatorsByWorkspace#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@getOperatorsByWorkspace#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
				: new ResponseEntity<>(listOperators, httpStatus);
	}

	@RequestMapping(value = "/municipalities/{municipalityId}/active", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get workspace active by municipality")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get workspace active by municipality", response = WorkspaceDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getWorkspaceActiveByMunicipality(@PathVariable Long municipalityId,
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

			MicroserviceRoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_ADMINISTRATOR).findAny().orElse(null);

			MicroserviceRoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_MANAGER).findAny().orElse(null);

			if (roleAdministrator instanceof MicroserviceRoleDto) {

				responseDto = workspaceBusiness.getWorkspaceActiveByMunicipality(municipalityId, null);

			} else if (roleManager instanceof MicroserviceRoleDto) {

				// verify that you have access to the municipality

				// get manager
				MicroserviceManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				responseDto = workspaceBusiness.getWorkspaceActiveByMunicipality(municipalityId, managerDto.getId());
			}

			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error(
					"Error WorkspaceV1Controller@getWorkspaceActiveByMunicipality#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@getWorkspaceActiveByMunicipality#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@getWorkspaceActiveByMunicipality#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/integration/{municipalityId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Make integration")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Integration done", response = BasicResponseDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<?> makeIntegrationAutomatic(@PathVariable Long municipalityId,
			@RequestBody MakeIntegrationDto requestMakeIntegration,
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

			// get manager
			MicroserviceManagerDto managerDto = null;
			MicroserviceManagerProfileDto profileDirector = null;
			try {
				managerDto = managerClient.findByUserCode(userDtoSession.getId());

				List<MicroserviceManagerProfileDto> managerProfiles = managerClient
						.findProfilesByUser(userDtoSession.getId());

				profileDirector = managerProfiles.stream()
						.filter(profileDto -> profileDto.getId() == RoleBusiness.SUB_ROLE_DIRECTOR).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de gestores.");
			}
			if (profileDirector == null) {
				throw new InputValidationException("Acceso denegado.");
			}

			// validation supply cadastre
			Long supplyCadastre = requestMakeIntegration.getSupplyCadastre();
			if (supplyCadastre == null || supplyCadastre <= 0) {
				throw new InputValidationException("El insumo de catastro es requerido.");
			}

			// validation supply registration
			Long supplyRegistration = requestMakeIntegration.getSupplyRegistration();
			if (supplyRegistration == null || supplyRegistration <= 0) {
				throw new InputValidationException("El insumo de registro es requerido.");
			}

			workspaceBusiness.makeIntegrationCadastreRegistration(municipalityId, supplyCadastre, supplyRegistration,
					managerDto, userDtoSession);

			httpStatus = HttpStatus.OK;
			responseDto = new BasicResponseDto("¡Se ha iniciado la integración!", 7);

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error WorkspaceV1Controller@makeIntegration#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@makeIntegration#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@makeIntegration#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "{workspaceId}/integrations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get integrations by workspace")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get integrations", response = IntegrationDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<?> getIntegrationsByWorkspace(@PathVariable Long workspaceId,
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

			MicroserviceRoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_ADMINISTRATOR).findAny().orElse(null);

			MicroserviceRoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_MANAGER).findAny().orElse(null);

			if (roleAdministrator instanceof MicroserviceRoleDto) {

				responseDto = integrationBusiness.getIntegrationsByWorkspace(workspaceId, null);

			} else if (roleManager instanceof MicroserviceRoleDto) {

				// get manager
				MicroserviceManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				responseDto = integrationBusiness.getIntegrationsByWorkspace(workspaceId, managerDto.getId());
			}

			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error WorkspaceV1Controller@getIntegrationsByWorkspace#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@getIntegrationsByWorkspace#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@getIntegrationsByWorkspace#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "{workspaceId}/integrations/{integrationId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get integrations by workspace")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Get integrations", response = BasicResponseDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<?> startIntegrationAssisted(@PathVariable Long workspaceId, @PathVariable Long integrationId,
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

			// get manager
			MicroserviceManagerDto managerDto = null;
			MicroserviceManagerProfileDto profileDirector = null;
			try {
				managerDto = managerClient.findByUserCode(userDtoSession.getId());

				List<MicroserviceManagerProfileDto> managerProfiles = managerClient
						.findProfilesByUser(userDtoSession.getId());

				profileDirector = managerProfiles.stream()
						.filter(profileDto -> profileDto.getId() == RoleBusiness.SUB_ROLE_DIRECTOR).findAny()
						.orElse(null);

			} catch (Exception e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de gestores.");
			}
			if (profileDirector == null) {
				throw new InputValidationException("Acceso denegado.");
			}

			responseDto = workspaceBusiness.startIntegrationAssisted(workspaceId, integrationId, managerDto,
					userDtoSession);
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error WorkspaceV1Controller@startIntegrationAssisted#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@startIntegrationAssisted#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@startIntegrationAssisted#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

}
