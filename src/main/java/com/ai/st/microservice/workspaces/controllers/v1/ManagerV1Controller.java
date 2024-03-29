package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.general.BasicResponseDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.ManagerMicroserviceBusiness;
import com.ai.st.microservice.workspaces.business.OperatorMicroserviceBusiness;
import com.ai.st.microservice.workspaces.dto.operators.CustomDeliveryDto;

import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
import com.ai.st.microservice.workspaces.services.tracing.TracingKeyword;
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

@Api(value = "Managers ", tags = { "Managers" })
@RestController
@RequestMapping("api/workspaces/v1/managers")
public class ManagerV1Controller {

    private final Logger log = LoggerFactory.getLogger(ManagerV1Controller.class);

    private final ManagerMicroserviceBusiness managerBusiness;
    private final OperatorMicroserviceBusiness operatorBusiness;
    private final AdministrationBusiness administrationBusiness;

    public ManagerV1Controller(ManagerMicroserviceBusiness managerBusiness,
            OperatorMicroserviceBusiness operatorBusiness, AdministrationBusiness administrationBusiness) {
        this.managerBusiness = managerBusiness;
        this.operatorBusiness = operatorBusiness;
        this.administrationBusiness = administrationBusiness;
    }

    @GetMapping(value = "/deliveries", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get deliveries by manager")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get deliveries by manager", response = CustomDeliveryDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getDeliveriesByManager(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getDeliveriesForManagerFromOperator");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
            if (managerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
            }

            if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para consultar entregas.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.MANAGER_ID, managerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.MANAGER_NAME, managerDto.getName());

            responseDto = operatorBusiness.getDeliveriesByManager(managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ManagerV1Controller@getDeliveriesByManager#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ManagerV1Controller@getDeliveriesByManager#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ManagerV1Controller@getDeliveriesByManager#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/deliveries/{deliveryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get delivery by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get deliveries by manager", response = CustomDeliveryDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getDeliveryById(@RequestHeader("authorization") String headerAuthorization,
            @PathVariable Long deliveryId) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getDeliveryByIdForManagerFromOperator");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
            if (managerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
            }

            if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para consultar entregas.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.MANAGER_ID, managerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.MANAGER_NAME, managerDto.getName());

            responseDto = operatorBusiness.getDeliveryIdAndManager(deliveryId, managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ManagerV1Controller@getDeliveryById#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ManagerV1Controller@getDeliveryById#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ManagerV1Controller@getDeliveryById#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/operators", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get operators by manager session")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operators got", response = MicroserviceOperatorDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getOperatorsByManager(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getOperatorsByManager");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
            if (managerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.MANAGER_ID, managerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.MANAGER_NAME, managerDto.getName());

            responseDto = managerBusiness.getOperatorsByManager(managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (Exception e) {
            log.error("Error ManagerV1Controller@getOperatorsByManager#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
