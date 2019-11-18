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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.business.DepartmentBusiness;
import com.ai.st.microservice.workspaces.business.MunicipalityBusiness;
import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

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
	private DepartmentBusiness departmentBusiness;

	@Autowired
	private MunicipalityBusiness municipalityBusiness;

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get departments")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get departments", response = DepartmentDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<List<DepartmentDto>> getDepartments() {

		HttpStatus httpStatus = null;
		List<DepartmentDto> listDeparments = new ArrayList<DepartmentDto>();

		try {

			listDeparments = departmentBusiness.getDepartments();

			httpStatus = HttpStatus.OK;
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
	public ResponseEntity<List<MunicipalityDto>> getMunicipalitiesById(@PathVariable Long departmentId) {

		HttpStatus httpStatus = null;
		List<MunicipalityDto> listMunicipalities = new ArrayList<MunicipalityDto>();

		try {

			listMunicipalities = municipalityBusiness.getMunicipalitiesByDepartmentId(departmentId);

			httpStatus = HttpStatus.OK;
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
