package com.ai.st.microservice.workspaces.controllers.v1;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.business.WorkspaceBusiness;
import com.ai.st.microservice.workspaces.dto.CreateWorkspaceDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;

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
	private WorkspaceBusiness workspaceBusiness;

	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create workspace")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Create Workspace", response = WorkspaceDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<WorkspaceDto> createWorkspace(@ModelAttribute CreateWorkspaceDto requestCreateWorkspace) {

		HttpStatus httpStatus = null;

		try {

			// validation end date
			String endDateString = requestCreateWorkspace.getEndDate();
			Date endDate = null;
			if (endDateString != null && !endDateString.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					endDate = sdf.parse(endDateString);
				} catch (Exception e) {
					throw new InputValidationException("The final date is invalid.");
				}
			} else {
				throw new InputValidationException("The final date is required.");
			}

			// validation manager code
			Long managerCode = requestCreateWorkspace.getManagerCode();
			if (managerCode == null || managerCode <= 0) {
				throw new InputValidationException("The manager code is required.");
			}

			// validation municipality
			Long municipalityId = requestCreateWorkspace.getMunicipalityId();
			if (municipalityId == null || municipalityId <= 0) {
				throw new InputValidationException("The municipality is required.");
			}

			// validation municipality
			String observations = requestCreateWorkspace.getObservations();
			if (observations == null || observations.isEmpty()) {
				throw new InputValidationException("The observations are required.");
			}

			// validation start date
			String startDateString = requestCreateWorkspace.getStartDate();
			Date startDate = null;
			if (startDateString != null && !startDateString.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					startDate = sdf.parse(startDateString);
				} catch (Exception e) {
					throw new InputValidationException("The start date is invalid.");
				}
			} else {
				throw new InputValidationException("The start date is required.");
			}

			// validation support
			MultipartFile supporFile = requestCreateWorkspace.getSupportFile();
			if (supporFile.isEmpty()) {
				throw new InputValidationException("The support file is required.");
			}

			// validation parcels number
			Long parcelsNumber = requestCreateWorkspace.getParcelsNumber();
			if (parcelsNumber != null) {
				if (parcelsNumber < 0) {
					throw new InputValidationException("The parcel number is invalid.");
				}
			}

			workspaceBusiness.createWorkspace(startDate, endDate, managerCode, municipalityId, observations,
					parcelsNumber);

			httpStatus = HttpStatus.CREATED;

		} catch (InputValidationException e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#Validation ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
		} catch (BusinessException e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
		} catch (Exception e) {
			log.error("Error WorkspaceV1Controller@createWorkspace#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(null, httpStatus);
	}

}
