package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.general.BasicResponseDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.ManagerMicroserviceBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.dto.supplies.CustomSupplyDto;

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

import java.util.List;

@Api(value = "Manage Supplies", tags = { "Supplies" })
@RestController
@RequestMapping("api/workspaces/v1/supplies")
public class SupplyV1Controller {

    private final Logger log = LoggerFactory.getLogger(SupplyV1Controller.class);

    private final SupplyBusiness supplyBusiness;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final AdministrationBusiness administrationBusiness;

    public SupplyV1Controller(SupplyBusiness supplyBusiness, ManagerMicroserviceBusiness managerBusiness,
            AdministrationBusiness administrationBusiness) {
        this.supplyBusiness = supplyBusiness;
        this.managerBusiness = managerBusiness;
        this.administrationBusiness = administrationBusiness;
    }

    @GetMapping(value = "/{municipalityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get supplies by municipality")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get supplies", response = CustomSupplyDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getSuppliesByMunicipality(@PathVariable Long municipalityId,
            @RequestParam(name = "extensions", required = false) List<String> extensions,
            @RequestHeader("authorization") String headerAuthorization,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "manager", required = false) Long managerCode,
            @RequestParam(name = "operator", required = false) Long operatorCode,
            @RequestParam(name = "active", defaultValue = "true") boolean active,
            @RequestParam(name = "requests", required = false) List<Long> requests) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession)) {

                responseDto = supplyBusiness.getSuppliesByMunicipalityAdmin(municipalityId, extensions, page, requests,
                        active, managerCode);

            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                responseDto = supplyBusiness.getSuppliesByMunicipalityManager(municipalityId, managerDto.getId(),
                        extensions, page, requests, active, operatorCode);
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

    @PutMapping(value = "/{supplyId}/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Active supply")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Supply updated", response = CustomSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> activeSupply(@PathVariable Long supplyId,
            @RequestHeader("authorization") String headerAuthorization) {

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

    @PutMapping(value = "/{supplyId}/inactive", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Inactive supply")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Supply updated", response = CustomSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> inactiveSupply(@PathVariable Long supplyId,
            @RequestHeader("authorization") String headerAuthorization) {

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
