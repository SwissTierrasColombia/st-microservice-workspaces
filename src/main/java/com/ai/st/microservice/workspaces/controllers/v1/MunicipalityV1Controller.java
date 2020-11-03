package com.ai.st.microservice.workspaces.controllers.v1;

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

import com.ai.st.microservice.workspaces.business.MunicipalityBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Municipalities", tags = { "Municipalities" })
@RestController
@RequestMapping("api/workspaces/v1/municipalities")
public class MunicipalityV1Controller {

	private final Logger log = LoggerFactory.getLogger(MunicipalityV1Controller.class);

	@Autowired
	private MunicipalityBusiness municipalityBusiness;

	@RequestMapping(value = "/by-manager/{managerId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get municipalities by manager")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get municipalities by manager", response = MunicipalityDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<?> getMunicipalitiesByManager(@PathVariable Long managerId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			responseDto = municipalityBusiness.getMunicipalitiesByManager(managerId);
			httpStatus = HttpStatus.OK;

		} catch (BusinessException e) {
			responseDto = new BasicResponseDto(e.getMessage(), 4);
			log.error("Error MunicipalityV1Controller@getMunicipalitiesByManager#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
		} catch (Exception e) {
			responseDto = new BasicResponseDto(e.getMessage(), 5);
			log.error("Error MunicipalityV1Controller@getMunicipalitiesByManager#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/not-workspace/departments/{departmentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get municipalities by department")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get municipalities not workspaces", response = MunicipalityDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<?> getMunicipalitiesNotWorkspaceByDepartment(@PathVariable Long departmentId,
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			responseDto = municipalityBusiness.getMunicipalitiesNotWorkspaceByDepartment(departmentId);
			httpStatus = HttpStatus.OK;

		} catch (BusinessException e) {
			responseDto = new BasicResponseDto(e.getMessage(), 4);
			log.error("Error MunicipalityV1Controller@getMunicipalitiesNotWorkspaceByDepartment#Business ---> "
					+ e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
		} catch (Exception e) {
			responseDto = new BasicResponseDto(e.getMessage(), 5);
			log.error("Error MunicipalityV1Controller@getMunicipalitiesNotWorkspaceByDepartment#General ---> "
					+ e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

}
