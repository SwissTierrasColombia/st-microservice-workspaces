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

import com.ai.st.microservice.workspaces.business.ManagerBusiness;
import com.ai.st.microservice.workspaces.business.OperatorBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;

import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Managers ", tags = {"Managers"})
@RestController
@RequestMapping("api/workspaces/v1/managers")
public class ManagerV1Controller {

    private final Logger log = LoggerFactory.getLogger(ManagerV1Controller.class);

    @Autowired
    private UserBusiness userBusiness;

    @Autowired
    private ManagerBusiness managerBusiness;

    @Autowired
    private OperatorBusiness operatorBusiness;

    @RequestMapping(value = "/deliveries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get deliveries by manager")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get deliveries by manager", response = MicroserviceDeliveryDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getDeliveriesByManager(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get manager
            MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
            if (managerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
            }

            if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para consultar entregas.");
            }

            responseDto = operatorBusiness.getDeliveriesByManager(managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ManagerV1Controller@getDeliveriesByManager#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error ManagerV1Controller@getDeliveriesByManager#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 2);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error ManagerV1Controller@getDeliveriesByManager#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/deliveries/{deliveryId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get delivery by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get deliveries by manager", response = MicroserviceDeliveryDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getDeliveriesById(@RequestHeader("authorization") String headerAuthorization,
                                               @PathVariable Long deliveryId) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get manager
            MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
            if (managerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
            }

            if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para consultar entregas.");
            }

            responseDto = operatorBusiness.getDeliveryIdAndManager(deliveryId, managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ManagerV1Controller@getDeliveriesById#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error ManagerV1Controller@getDeliveriesById#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 2);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error ManagerV1Controller@getDeliveriesById#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
