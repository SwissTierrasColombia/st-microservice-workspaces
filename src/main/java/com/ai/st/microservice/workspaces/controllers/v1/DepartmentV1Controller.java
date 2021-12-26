package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.*;
import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;

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

import java.util.ArrayList;
import java.util.List;

@Api(value = "Manage Departments", tags = {"Departments"})
@RestController
@RequestMapping("api/workspaces/v1/departments")
public class DepartmentV1Controller {

    private final Logger log = LoggerFactory.getLogger(DepartmentV1Controller.class);

    private final DepartmentBusiness departmentBusiness;
    private final MunicipalityBusiness municipalityBusiness;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final AdministrationBusiness administrationBusiness;

    public DepartmentV1Controller(DepartmentBusiness departmentBusiness, MunicipalityBusiness municipalityBusiness,
                                  ManagerMicroserviceBusiness managerBusiness, AdministrationBusiness administrationBusiness) {
        this.departmentBusiness = departmentBusiness;
        this.municipalityBusiness = municipalityBusiness;
        this.managerBusiness = managerBusiness;
        this.administrationBusiness = administrationBusiness;
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get departments")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get departments", response = DepartmentDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<List<DepartmentDto>> getDepartments(
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        List<DepartmentDto> listDepartments = new ArrayList<>();

        try {

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession) || administrationBusiness.isProvider(userDtoSession)) {
                listDepartments = departmentBusiness.getDepartments();
            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                listDepartments = departmentBusiness.getDepartmentsByManagerCode(managerDto.getId());
            }

            httpStatus = HttpStatus.OK;
        } catch (DisconnectedMicroserviceException e) {
            log.error("Error DepartmentV1Controller@getDepartments#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            listDepartments = null;
            log.error("Error DepartmentV1Controller@getDepartments#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            listDepartments = null;
            log.error("Error DepartmentV1Controller@getDepartments#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(listDepartments, httpStatus);
    }

    @GetMapping(value = "/{departmentId}/municipalities", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get municipalities by department")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get municipalities by department", response = MunicipalityDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<List<MunicipalityDto>> getMunicipalitiesById(@PathVariable Long departmentId,
                                                                       @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        List<MunicipalityDto> listMunicipalities = new ArrayList<>();

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession) || administrationBusiness.isProvider(userDtoSession)) {
                listMunicipalities = municipalityBusiness.getMunicipalitiesByDepartmentId(departmentId);
            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
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
