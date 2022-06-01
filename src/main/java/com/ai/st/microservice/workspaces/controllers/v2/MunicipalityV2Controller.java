package com.ai.st.microservice.workspaces.controllers.v2;

import com.ai.st.microservice.common.dto.general.BasicResponseDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.MunicipalityBusiness;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;

import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Municipalities", tags = { "Municipalities" })
@RestController
@RequestMapping("api/workspaces/v2/municipalities")
public class MunicipalityV2Controller {

    private final Logger log = LoggerFactory.getLogger(MunicipalityV2Controller.class);

    private final MunicipalityBusiness municipalityBusiness;

    public MunicipalityV2Controller(MunicipalityBusiness municipalityBusiness) {
        this.municipalityBusiness = municipalityBusiness;
    }

    @GetMapping(value = "/{managerCode}/not-belong/departments/{departmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get municipalities where manager does not belong in")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get municipalities", response = MunicipalityDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getMunicipalitiesWhereManagerDoesNotBelongIn(@PathVariable Long managerCode,
            @PathVariable Long departmentId) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getMunicipalitiesWhereManagerDoesNotBelongIn");

            responseDto = municipalityBusiness.getMunicipalitiesWhereManagerDoesNotBelong(managerCode, departmentId);
            httpStatus = HttpStatus.OK;

        } catch (BusinessException e) {
            log.error("Error MunicipalityV2Controller@getMunicipalitiesWhereManagerDoesntBelongIn#Business ---> "
                    + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error MunicipalityV2Controller@getMunicipalitiesWhereManagerDoesntBelongIn#General ---> "
                    + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
