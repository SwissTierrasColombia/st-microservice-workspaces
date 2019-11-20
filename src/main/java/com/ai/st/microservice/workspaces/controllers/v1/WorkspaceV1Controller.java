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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.business.RoleBusiness;
import com.ai.st.microservice.workspaces.business.WorkspaceBusiness;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.CreateWorkspaceDto;
import com.ai.st.microservice.workspaces.dto.ErrorDto;
import com.ai.st.microservice.workspaces.dto.ManagerDto;
import com.ai.st.microservice.workspaces.dto.RoleDto;
import com.ai.st.microservice.workspaces.dto.UserDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
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
	private WorkspaceBusiness workspaceBusiness;

	@Autowired
	private UserFeignClient userClient;

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

	@RequestMapping(value = "/municipalities/{municipalityId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get workspace by municipality")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Get workspaces by municipality", response = WorkspaceDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<List<WorkspaceDto>> getWorkspacesByMunicipality(@PathVariable Long municipalityId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		List<WorkspaceDto> listWorkspaces = new ArrayList<WorkspaceDto>();

		try {

			// user session
			String token = headerAuthorization.replace("Bearer ", "").trim();
			UserDto userDtoSession = null;
			try {
				userDtoSession = userClient.findByToken(token);
			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de usuarios.");
			}

			RoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_ADMINISTRATOR).findAny().orElse(null);

			RoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_MANAGER).findAny().orElse(null);

			if (roleAdministrator instanceof RoleDto) {

				listWorkspaces = workspaceBusiness.getWorkspacesByMunicipality(municipalityId, null);

			} else if (roleManager instanceof RoleDto) {

				// verify that you have access to the municipality

				// get manager
				ManagerDto managerDto = null;
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

}
