package com.ai.st.microservice.workspaces.controllers.v1;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.business.DepartmentBusiness;
import com.ai.st.microservice.workspaces.business.MunicipalityBusiness;
import com.ai.st.microservice.workspaces.business.RoleBusiness;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.ManagerDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.RoleDto;
import com.ai.st.microservice.workspaces.dto.UserDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;

import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Departments", description = "Manage Departments", tags = { "Departments" })
@RestController
@RequestMapping("api/workspaces/v1/departments")
public class DepartmentV1Controller {

	private final Logger log = LoggerFactory.getLogger(DepartmentV1Controller.class);

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private ManagerFeignClient managerClient;

	@Autowired
	private DepartmentBusiness departmentBusiness;

	@Autowired
	private MunicipalityBusiness municipalityBusiness;

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get departments")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get departments", response = DepartmentDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<List<DepartmentDto>> getDepartments(
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		List<DepartmentDto> listDeparments = new ArrayList<DepartmentDto>();

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
				listDeparments = departmentBusiness.getDepartments();
			} else if (roleManager instanceof RoleDto) {

				// get manager
				ManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				listDeparments = departmentBusiness.getDepartmentsByManagerCode(managerDto.getId());
			}

			httpStatus = HttpStatus.OK;
		} catch (DisconnectedMicroserviceException e) {
			log.error("Error DepartmentV1Controller@getMunicipalitiesById#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		} catch (BusinessException e) {
			listDeparments = null;
			log.error("Error DepartmentV1Controller@getDepartments#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
		} catch (Exception e) {
			listDeparments = null;
			log.error("Error DepartmentV1Controller@getDepartments#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(listDeparments, httpStatus);
	}

	@RequestMapping(value = "/{departmentId}/municipalities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get municipalities by department")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get municipalities by department", response = MunicipalityDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<List<MunicipalityDto>> getMunicipalitiesById(@PathVariable Long departmentId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		List<MunicipalityDto> listMunicipalities = new ArrayList<MunicipalityDto>();

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
				listMunicipalities = municipalityBusiness.getMunicipalitiesByDepartmentId(departmentId);
			} else if (roleManager instanceof RoleDto) {

				// get manager
				ManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				listMunicipalities = municipalityBusiness.getMunicipalitiesByDepartmentIdAndManager(departmentId,
						managerDto.getId());
			}

			httpStatus = HttpStatus.OK;
		} catch (DisconnectedMicroserviceException e) {
			log.error("Error DepartmentV1Controller@getMunicipalitiesById#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		} catch (BusinessException e) {
			listMunicipalities = null;
			log.error("Error DepartmentV1Controller@getMunicipalitiesById#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
		} catch (Exception e) {
			listMunicipalities = null;
			log.error("Error DepartmentV1Controller@getMunicipalitiesById#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(listMunicipalities, httpStatus);
	}

}
