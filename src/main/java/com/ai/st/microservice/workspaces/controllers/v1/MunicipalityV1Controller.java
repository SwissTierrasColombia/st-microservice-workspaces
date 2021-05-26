package com.ai.st.microservice.workspaces.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ai.st.microservice.workspaces.business.MunicipalityBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Municipalities", tags = {"Municipalities"})
@RestController
@RequestMapping("api/workspaces/v1/municipalities")
public class MunicipalityV1Controller {

    private final Logger log = LoggerFactory.getLogger(MunicipalityV1Controller.class);

    private final MunicipalityBusiness municipalityBusiness;

    public MunicipalityV1Controller(MunicipalityBusiness municipalityBusiness) {
        this.municipalityBusiness = municipalityBusiness;
    }

    @RequestMapping(value = "/by-manager/{managerId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get municipalities by manager")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get municipalities by manager", response = MunicipalityDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getMunicipalitiesByManager(@PathVariable Long managerId) {

        HttpStatus httpStatus;
        Object responseDto;

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
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getMunicipalitiesNotWorkspaceByDepartment(@PathVariable Long departmentId) {

        HttpStatus httpStatus;
        Object responseDto;

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

    @GetMapping(value = "/code/{municipalityCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get municipality by code")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Municipality got", response = MunicipalityDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getMunicipalityById(@PathVariable String municipalityCode) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            responseDto = municipalityBusiness.getMunicipalityByCode(municipalityCode);
            httpStatus = HttpStatus.OK;

        } catch (Exception e) {
            responseDto = new BasicResponseDto(e.getMessage(), 5);
            log.error("Error MunicipalityV1Controller@getMunicipalityById#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
