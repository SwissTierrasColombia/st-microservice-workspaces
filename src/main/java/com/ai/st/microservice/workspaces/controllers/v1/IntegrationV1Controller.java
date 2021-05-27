package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.ManagerMicroserviceBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.IntegrationDto;
import com.ai.st.microservice.workspaces.dto.PossibleIntegrationDto;

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

@Api(value = "Manage Integrations", tags = {"Integrations"})
@RestController
@RequestMapping("api/workspaces/v1/integrations")
public class IntegrationV1Controller {

    private final Logger log = LoggerFactory.getLogger(IntegrationV1Controller.class);

    private final ManagerMicroserviceBusiness managerBusiness;
    private final IntegrationBusiness integrationBusiness;
    private final AdministrationBusiness administrationBusiness;

    public IntegrationV1Controller(ManagerMicroserviceBusiness managerBusiness, IntegrationBusiness integrationBusiness,
                                   AdministrationBusiness administrationBusiness) {
        this.managerBusiness = managerBusiness;
        this.integrationBusiness = integrationBusiness;
        this.administrationBusiness = administrationBusiness;
    }

    @GetMapping(value = "/running", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get integrations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get integrations", response = IntegrationDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> getIntegrationsRunning(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get manager
            MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
            if (managerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
            }

            if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para consultar las integraciones.");
            }

            responseDto = integrationBusiness.getIntegrationsRunning(managerDto);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error IntegrationV1Controller@getIntegrationsRunning#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error IntegrationV1Controller@getIntegrationsRunning#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error IntegrationV1Controller@getIntegrationsRunning#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PutMapping(value = "{integrationId}/configure-view", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get integrations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get integrations", response = PossibleIntegrationDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> configureViewIntegration(@RequestHeader("authorization") String headerAuthorization,
                                                           @PathVariable Long integrationId) {
        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get manager
            MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
            if (managerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
            }

            if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para configurar las integraciones.");
            }

            integrationBusiness.configureViewIntegration(integrationId, managerDto.getId());
            responseDto = new BasicResponseDto("Se ha configurado ", 7);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error IntegrationV1Controller@configureViewIntegration#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error IntegrationV1Controller@configureViewIntegration#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error IntegrationV1Controller@configureViewIntegration#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
