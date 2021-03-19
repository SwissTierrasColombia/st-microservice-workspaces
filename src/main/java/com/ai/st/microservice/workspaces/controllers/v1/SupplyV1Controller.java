package com.ai.st.microservice.workspaces.controllers.v1;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.business.ManagerBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Supplies", tags = {"Supplies"})
@RestController
@RequestMapping("api/workspaces/v1/supplies")
public class SupplyV1Controller {

    private final Logger log = LoggerFactory.getLogger(SupplyV1Controller.class);

    @Autowired
    private UserFeignClient userClient;

    @Autowired
    private ManagerFeignClient managerClient;

    @Autowired
    private SupplyBusiness supplyBusiness;

    @Autowired
    private UserBusiness userBusiness;

    @Autowired
    private ManagerBusiness managerBusiness;

    @RequestMapping(value = "/{municipalityId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get supplies by municipality")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get supplies", response = MicroserviceSupplyDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getSuppliesByMunicipality(@PathVariable Long municipalityId,
                                                       @RequestParam(name = "extensions", required = false) List<String> extensions,
                                                       @RequestHeader("authorization") String headerAuthorization,
                                                       @RequestParam(name = "page", required = false) Integer page,
                                                       @RequestParam(name = "manager", required = false) Long managerCode,
                                                       @RequestParam(name = "active", required = true, defaultValue = "true") boolean active,
                                                       @RequestParam(name = "requests", required = false) List<Long> requests) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (userBusiness.isAdministrator(userDtoSession)) {

                responseDto = supplyBusiness.getSuppliesByMunicipalityAdmin(municipalityId, extensions, page, requests,
                        active, managerCode);

            } else if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                responseDto = supplyBusiness.getSuppliesByMunicipalityManager(municipalityId, managerDto.getId(),
                        extensions, page, requests, active);
            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error SupplyV1Controller@getSuppliesByMunicipality#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error SupplyV1Controller@getSuppliesByMunicipality#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error SupplyV1Controller@getSuppliesByMunicipality#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/{supplyId}/active", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Active supply")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Supply updated", response = MicroserviceSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> activeSupply(@PathVariable Long supplyId,
                                          @RequestHeader("authorization") String headerAuthorization) {

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
                throw new InputValidationException("El usuario no tiene permisos para activar insumos.");
            }

            responseDto = supplyBusiness.changeStateSupply(supplyId, SupplyBusiness.SUPPLY_STATE_ACTIVE,
                    managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error SupplyV1Controller@activeSupply#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 5);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error SupplyV1Controller@activeSupply#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error SupplyV1Controller@activeSupply#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/{supplyId}/inactive", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Inactive supply")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Supply updated", response = MicroserviceSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> inactiveSupply(@PathVariable Long supplyId,
                                            @RequestHeader("authorization") String headerAuthorization) {

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
                throw new InputValidationException("El usuario no tiene permisos para activar insumos.");
            }

            responseDto = supplyBusiness.changeStateSupply(supplyId, SupplyBusiness.SUPPLY_STATE_INACTIVE,
                    managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error SupplyV1Controller@inactiveSupply#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 5);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error SupplyV1Controller@inactiveSupply#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error SupplyV1Controller@inactiveSupply#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
