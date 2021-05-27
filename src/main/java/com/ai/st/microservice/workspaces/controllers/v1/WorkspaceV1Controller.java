package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceSupplyAttachmentDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.ManagerMicroserviceBusiness;
import com.ai.st.microservice.workspaces.business.MunicipalityBusiness;
import com.ai.st.microservice.workspaces.business.OperatorMicroserviceBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.business.WorkspaceBusiness;
import com.ai.st.microservice.workspaces.business.WorkspaceOperatorBusiness;
import com.ai.st.microservice.workspaces.dto.AssignOperatorWorkpaceDto;
import com.ai.st.microservice.workspaces.dto.IntegrationDto;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.CreateDeliveryDto;
import com.ai.st.microservice.workspaces.dto.CreateSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.MakeIntegrationDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.utils.ZipUtil;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletContext;

@Api(value = "Manage Workspaces", tags = {"Workspaces"})
@RestController
@RequestMapping("api/workspaces/v1/workspaces")
public class WorkspaceV1Controller {

    private final Logger log = LoggerFactory.getLogger(WorkspaceV1Controller.class);

    @Value("${st.temporalDirectory}")
    private String stTemporalDirectory;

    private final WorkspaceBusiness workspaceBusiness;
    private final IntegrationBusiness integrationBusiness;
    private final SupplyBusiness supplyBusiness;
    private final OperatorMicroserviceBusiness operatorBusiness;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final MunicipalityBusiness municipalityBusiness;
    private final WorkspaceOperatorBusiness workspaceOperatorBusiness;
    private final ServletContext servletContext;
    private final AdministrationBusiness administrationBusiness;

    public WorkspaceV1Controller(WorkspaceBusiness workspaceBusiness, IntegrationBusiness integrationBusiness,
                                 SupplyBusiness supplyBusiness, OperatorMicroserviceBusiness operatorBusiness,
                                 ManagerMicroserviceBusiness managerBusiness, MunicipalityBusiness municipalityBusiness,
                                 WorkspaceOperatorBusiness workspaceOperatorBusiness, ServletContext servletContext,
                                 AdministrationBusiness administrationBusiness) {
        this.workspaceBusiness = workspaceBusiness;
        this.integrationBusiness = integrationBusiness;
        this.supplyBusiness = supplyBusiness;
        this.operatorBusiness = operatorBusiness;
        this.managerBusiness = managerBusiness;
        this.municipalityBusiness = municipalityBusiness;
        this.workspaceOperatorBusiness = workspaceOperatorBusiness;
        this.servletContext = servletContext;
        this.administrationBusiness = administrationBusiness;
    }

    @GetMapping(value = "/municipalities/{municipalityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get workspaces by municipality")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get workspaces by municipality", response = WorkspaceDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<List<WorkspaceDto>> getWorkspacesByMunicipality(@PathVariable Long municipalityId,
                                                                          @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        List<WorkspaceDto> listWorkspaces = new ArrayList<>();

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession)) {

                listWorkspaces = workspaceBusiness.getWorkspacesByMunicipality(municipalityId, null);

            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                listWorkspaces = workspaceBusiness.getWorkspacesByMunicipality(municipalityId, managerDto.getId());
            }

            httpStatus = HttpStatus.CREATED;
        } catch (DisconnectedMicroserviceException e) {
            listWorkspaces = null;
            log.error("Error WorkspaceV1Controller@getWorkspacesByMunicipality#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            listWorkspaces = null;
            log.error("Error WorkspaceV1Controller@getWorkspacesByMunicipality#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            listWorkspaces = null;
            log.error("Error WorkspaceV1Controller@getWorkspacesByMunicipality#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(listWorkspaces, httpStatus);
    }

    @PostMapping(value = "/{workspaceId}/operators", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Assign operator to workspace (municipality)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Assign operator to workspace", response = WorkspaceDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> assignOperator(@PathVariable Long workspaceId,
                                                 @ModelAttribute AssignOperatorWorkpaceDto requestAssignOperator,
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
                throw new InputValidationException("El usuario no tiene permisos para descargar el soporte.");
            }

            // validation start date
            String startDateString = requestAssignOperator.getStartDate();
            Date startDate;
            if (startDateString != null && !startDateString.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    startDate = sdf.parse(startDateString);
                } catch (Exception e) {
                    throw new InputValidationException("La fecha de inicio es inválida.");
                }
            } else {
                throw new InputValidationException("La fecha de inicio es requerida.");
            }

            // validation end date
            String endDateString = requestAssignOperator.getEndDate();
            Date endDate;
            if (endDateString != null && !endDateString.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    endDate = sdf.parse(endDateString);
                } catch (Exception e) {
                    throw new InputValidationException("La fecha de finalización es inválida.");
                }
            } else {
                throw new InputValidationException("La fecha de finalización es requerida.");
            }

            // validation operator code
            Long operatorCode = requestAssignOperator.getOperatorCode();
            if (operatorCode == null || operatorCode <= 0) {
                throw new InputValidationException("El operador es requerido.");
            }

            // validation support
            MultipartFile supportFile = requestAssignOperator.getSupportFile();
            if (supportFile.isEmpty()) {
                throw new InputValidationException("El archivo de soporte es requerido.");
            }

            // validation number parcels expected
            Long parcelsNumber = requestAssignOperator.getNumberParcelsExpected();
            if (parcelsNumber != null) {
                if (parcelsNumber < 0) {
                    throw new InputValidationException("El número de predios es inválido.");
                }
            }

            // validation municipality area
            Double workArea = requestAssignOperator.getWorkArea();
            if (workArea != null) {
                if (workArea < 0) {
                    throw new InputValidationException("El área es inválida.");
                }
            }

            responseDto = workspaceBusiness.assignOperator(workspaceId, startDate, endDate, operatorCode, parcelsNumber,
                    workArea, supportFile, requestAssignOperator.getObservations(), managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@assignOperator#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error WorkspaceV1Controller@assignOperator#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@assignOperator#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@assignOperator#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/{workspaceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get workspace by id")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Get workspace by id", response = WorkspaceDto.class),
            @ApiResponse(code = 404, message = "Workspace not found", response = String.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> getWorkspaceById(@PathVariable Long workspaceId,
                                                   @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession)) {

                responseDto = workspaceBusiness.getWorkspaceById(workspaceId, null);

            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                responseDto = workspaceBusiness.getWorkspaceById(workspaceId, managerDto.getId());
            }

            httpStatus = (responseDto != null) ? HttpStatus.OK : HttpStatus.NOT_FOUND;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@getWorkspaceById#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@getWorkspaceById#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getWorkspaceById#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/{workspaceId}/operators", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get operators by workspace")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get operators by workspace", response = WorkspaceDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getOperatorsByWorkspace(@PathVariable Long workspaceId,
                                                     @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        List<WorkspaceOperatorDto> listOperators = new ArrayList<>();
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession)) {

                listOperators = workspaceBusiness.getOperatorsByWorkspaceId(workspaceId, null);

            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                listOperators = workspaceBusiness.getOperatorsByWorkspaceId(workspaceId, managerDto.getId());
            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@getOperatorsByWorkspace#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@getOperatorsByWorkspace#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getOperatorsByWorkspace#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
                : new ResponseEntity<>(listOperators, httpStatus);
    }

    @GetMapping(value = "/municipalities/{municipalityId}/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get workspace active by municipality")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get workspace active by municipality", response = WorkspaceDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> getWorkspaceActiveByMunicipality(@PathVariable Long municipalityId,
                                                                   @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession)) {

                responseDto = workspaceBusiness.getWorkspaceActiveByMunicipality(municipalityId, null);

            } else if (administrationBusiness.isManager(userDtoSession)) {

                // verify that you have access to the municipality
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para consultar el espacio de trabajo.");
                }

                responseDto = workspaceBusiness.getWorkspaceActiveByMunicipality(municipalityId, managerDto.getId());
            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@getWorkspaceActiveByMunicipality#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@getWorkspaceActiveByMunicipality#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getWorkspaceActiveByMunicipality#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PostMapping(value = "/integration/{municipalityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Make integration")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Integration done", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> makeIntegrationAutomatic(@PathVariable Long municipalityId,
                                                      @RequestBody MakeIntegrationDto requestMakeIntegration,
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
                throw new InputValidationException("El usuario no tiene permisos para realizar la integración.");
            }

            // validation supply cadastre
            Long supplyCadastre = requestMakeIntegration.getSupplyCadastre();
            if (supplyCadastre == null || supplyCadastre <= 0) {
                throw new InputValidationException("El insumo de catastro es requerido.");
            }

            // validation supply registration
            Long supplyRegistration = requestMakeIntegration.getSupplyRegistration();
            if (supplyRegistration == null || supplyRegistration <= 0) {
                throw new InputValidationException("El insumo de registro es requerido.");
            }

            responseDto = workspaceBusiness.makeIntegrationCadastreRegistration(municipalityId, supplyCadastre,
                    supplyRegistration, managerDto, userDtoSession);

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@makeIntegration#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@makeIntegration#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@makeIntegration#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "{workspaceId}/integrations", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get integrations by workspace")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get integrations", response = IntegrationDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getIntegrationsByWorkspace(@PathVariable Long workspaceId,
                                                        @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession)) {

                responseDto = integrationBusiness.getIntegrationsByWorkspace(workspaceId, null);

            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                responseDto = integrationBusiness.getIntegrationsByWorkspace(workspaceId, managerDto.getId());
            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@getIntegrationsByWorkspace#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@getIntegrationsByWorkspace#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getIntegrationsByWorkspace#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PostMapping(value = "{workspaceId}/integrations/{integrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Start integration assisted")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Get integrations", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> startIntegrationAssisted(@PathVariable Long workspaceId, @PathVariable Long integrationId,
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
                throw new InputValidationException("El usuario no tiene permisos para realizar la integración asistida.");
            }

            responseDto = workspaceBusiness.startIntegrationAssisted(workspaceId, integrationId, managerDto,
                    userDtoSession);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@startIntegrationAssisted#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@startIntegrationAssisted#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@startIntegrationAssisted#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PostMapping(value = "{workspaceId}/integrations/{integrationId}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Generate supply from integration")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Supply generated", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> generateSupply(@PathVariable Long workspaceId, @PathVariable Long integrationId,
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
                throw new InputValidationException("El usuario no tiene permisos para actualizar el soporte.");
            }

            responseDto = workspaceBusiness.exportXtf(workspaceId, integrationId, managerDto, userDtoSession);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@generateSupply#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@generateSupply#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@generateSupply#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @DeleteMapping(value = "{workspaceId}/integrations/{integrationId}")
    @ApiOperation(value = "Remove integration from workspace")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Integration deleted", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> removeIntegrationFromWorkspace(@PathVariable Long workspaceId,
                                                            @PathVariable Long integrationId,
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
                throw new InputValidationException("El usuario no tiene permisos para actualizar el soporte.");
            }

            workspaceBusiness.removeIntegrationFromWorkspace(workspaceId, integrationId, managerDto.getId());
            responseDto = new BasicResponseDto("Se ha borrado la integración", 7);
            httpStatus = HttpStatus.NO_CONTENT;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@removeIntegrationFromWorkspace#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@removeIntegrationFromWorkspace#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@removeIntegrationFromWorkspace#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "download-supply/{supplyId}")
    @ApiOperation(value = "Download file")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "File downloaded", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> downloadSupply(@PathVariable Long supplyId,
                                            @RequestHeader("authorization") String headerAuthorization) {

        MediaType mediaType;
        File file;
        InputStreamResource resource;
        MicroserviceSupplyDto supplyDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            supplyDto = supplyBusiness.getSupplyById(supplyId);
            if (supplyDto == null) {
                throw new BusinessException("No se ha encontrado el insumo.");
            }

            if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para actualizar el soporte.");
                }

                if (!workspaceBusiness.managerHasAccessToMunicipality(supplyDto.getMunicipalityCode(), managerDto.getId())) {
                    throw new InputValidationException("El gestor no tiene acceso al insumo.");
                }

                if (!supplyDto.getManagerCode().equals(managerDto.getId())) {
                    throw new BusinessException("No tiene acceso al insumo");
                }

            } else if (administrationBusiness.isOperator(userDtoSession)) {

                // get operator
                MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorByUserCode(userDtoSession.getId());

                MicroserviceDeliveryDto deliveryDto = workspaceOperatorBusiness.getDeliveryFromSupply(operatorDto.getId(), supplyDto.getId());
                if (deliveryDto == null) {
                    throw new InputValidationException("El operador no tiene acceso al insumo.");
                }

                workspaceOperatorBusiness.registerDownloadSupply(deliveryDto, supplyDto.getId(), userDtoSession.getId());

            }

            String pathFile;

            MicroserviceSupplyAttachmentDto attachmentFtp = supplyDto.getAttachments().stream()
                    .filter(a -> a.getAttachmentType().getId().equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_FTP))
                    .findAny().orElse(null);

            MicroserviceSupplyAttachmentDto attachmentSupply = supplyDto.getAttachments().stream()
                    .filter(a -> a.getAttachmentType().getId().equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY) || a
                            .getAttachmentType().getId().equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_EXTERNAL_SOURCE))
                    .findAny().orElse(null);

            MunicipalityDto municipalityDto = municipalityBusiness.getMunicipalityByCode(supplyDto.getMunicipalityCode());

            // the supply has FTP
            if (attachmentFtp != null && attachmentSupply == null) {

                File fileFTP = supplyBusiness.generateFTPFile(supplyDto, municipalityDto);

                String randomCode = RandomStringUtils.random(10, false, true);
                pathFile = ZipUtil.zipping(new ArrayList<>(Collections.singletonList(fileFTP)), "insumo_" + randomCode, stTemporalDirectory);

            }
            // the supply has file to download
            else if (attachmentFtp == null && attachmentSupply != null) {
                pathFile = attachmentSupply.getData();
            } else { // the supply has both attachments types (file and FTP)

                File fileFTP = supplyBusiness.generateFTPFile(supplyDto, municipalityDto);
                File fileSupply = new File(attachmentSupply.getData());

                String randomCode = RandomStringUtils.random(10, false, true);
                pathFile = ZipUtil.zipping(new ArrayList<>(Arrays.asList(fileFTP, fileSupply)),
                        "insumo_" + randomCode, stTemporalDirectory);

            }

            Path path = Paths.get(pathFile);
            String fileName = path.getFileName().toString();

            String mineType = servletContext.getMimeType(fileName);

            try {
                mediaType = MediaType.parseMediaType(mineType);
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            file = new File(pathFile);
            resource = new InputStreamResource(new FileInputStream(file));

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@downloadSupply#Microservice ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 4), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@downloadSupply#Business ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 2), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@downloadSupply#General ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 3), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + supplyDto.getMunicipalityCode() + "_" + supplyDto.getName())
                .contentType(mediaType).contentLength(file.length())
                .header("extension", Files.getFileExtension(file.getName()))
                .header("filename", supplyDto.getMunicipalityCode() + "_" + supplyDto.getName()
                        + Files.getFileExtension(file.getName()))
                .body(resource);

    }

    @DeleteMapping(value = "{workspaceId}/supplies/{supplyId}")
    @ApiOperation(value = "Delete supply")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Supply Deleted", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> removeSupply(@PathVariable Long workspaceId, @PathVariable Long supplyId,
                                          @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession)) {

                workspaceBusiness.removeSupply(workspaceId, supplyId, null);

            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para activar insumos.");
                }

                workspaceBusiness.removeSupply(workspaceId, supplyId, managerDto.getId());
            }

            responseDto = new BasicResponseDto("Se ha eliminado el insumo", 7);
            httpStatus = HttpStatus.NO_CONTENT;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@removeSupply#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@removeSupply#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 2);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@removeSupply#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PostMapping(value = "{workspaceId}/operators/deliveries")
    @ApiOperation(value = "Create delivery")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Delivery created", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> createDelivery(@PathVariable Long workspaceId,
                                            @RequestHeader("authorization") String headerAuthorization,
                                            @RequestBody CreateDeliveryDto createDeliveryDto) {

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
                throw new InputValidationException("El usuario no tiene permisos para actualizar el soporte.");
            }

            // validation observations
            String observations = createDeliveryDto.getObservations();
            if (observations == null || observations.isEmpty()) {
                throw new InputValidationException("Las observaciones son requeridas.");
            }

            // validation supplies
            List<CreateSupplyDeliveryDto> supplies = createDeliveryDto.getSupplies();
            if (supplies == null || supplies.size() == 0) {
                throw new InputValidationException("Los insumos son requeridos.");
            } else {
                for (CreateSupplyDeliveryDto supplyDto : supplies) {
                    if (supplyDto.getSupplyId() == null || supplyDto.getSupplyId() <= 0) {
                        throw new InputValidationException("El código del insumo es requerido.");
                    }
                }
            }

            // validation operator
            Long operatorCode = createDeliveryDto.getOperatorCode();
            if (operatorCode == null || operatorCode <= 0) {
                throw new InputValidationException("Es necesario definir a cuál operador se le realizará la entrega.");
            }

            responseDto = workspaceBusiness.createDelivery(workspaceId, managerDto.getId(), operatorCode, observations, supplies);
            httpStatus = HttpStatus.CREATED;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@createDelivery#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (InputValidationException e) {
            log.error("Error WorkspaceV1Controller@createDelivery#Validation ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.BAD_REQUEST;
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@createDelivery#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 2);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@createDelivery#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "operators/deliveries")
    @ApiOperation(value = "Get deliveries")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Get deliveries", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getSuppliesOperator(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get operator
            MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorByUserCode(userDtoSession.getId());
            if (operatorDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el operador.");
            }

            responseDto = operatorBusiness.getDeliveriesActivesByOperator(operatorDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@getSuppliesOperator#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@getSuppliesOperator#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 2);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getSuppliesOperator#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "operators/deliveries/closed")
    @ApiOperation(value = "Get deliveries closed")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get deliveries closed", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getDeliveriesClosed(@RequestHeader("authorization") String headerAuthorization,
                                                 @RequestParam(required = false, name = "municipality") Long municipalityId) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get operator
            MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorByUserCode(userDtoSession.getId());
            if (operatorDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el operador.");
            }

            responseDto = operatorBusiness.getDeliveriesClosedByOperator(operatorDto.getId(), municipalityId);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@getDeliveriesClosed#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 4);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@getDeliveriesClosed#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 2);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getDeliveriesClosed#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/location")
    @ApiOperation(value = "Get workspaces by location")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get workspaces", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getWorkspacesByLocation(@RequestHeader("authorization") String headerAuthorization,
                                                     @RequestParam(name = "department") Long departmentId,
                                                     @RequestParam(required = false, name = "municipality") Long municipalityId) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (administrationBusiness.isAdministrator(userDtoSession)) {

                responseDto = workspaceBusiness.getWorkspacesByLocation(departmentId, municipalityId, null);

            } else if (administrationBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                responseDto = workspaceBusiness.getWorkspacesByLocation(departmentId, municipalityId, managerDto.getId());
            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@getWorkspacesByLocation#Microservice ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 2);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@getWorkspacesByLocation#Business ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 2);
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getWorkspacesByLocation#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage(), 3);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/report-delivery/{deliveryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Download report delivery")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Download report delivery"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> reportDownloadDeliveryManager(@PathVariable Long deliveryId,
                                                           @RequestHeader("authorization") String headerAuthorization) {

        MediaType mediaType;
        File file;
        InputStreamResource resource;

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
                throw new InputValidationException("El usuario no tiene permisos para crear peticiones.");
            }

            String pathFile = workspaceOperatorBusiness.generateReportDeliveryManager(managerDto.getId(), deliveryId);

            Path path = Paths.get(pathFile);
            String fileName = path.getFileName().toString();

            String mineType = servletContext.getMimeType(fileName);

            try {
                mediaType = MediaType.parseMediaType(mineType);
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            file = new File(pathFile);
            resource = new InputStreamResource(new FileInputStream(file));

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error OperatorV1Controller@reportDownloadDeliveryManager#Microservice ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 4), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (BusinessException e) {
            log.error("Error OperatorV1Controller@reportDownloadDeliveryManager#Business ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 2), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            log.error("Error OperatorV1Controller@reportDownloadDeliveryManager#General ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 3), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType).contentLength(file.length())
                .header("extension", Files.getFileExtension(file.getName()))
                .header("filename", "reporte_entrega." + Files.getFileExtension(file.getName())).body(resource);
    }

    @DeleteMapping(value = "/unassign/{municipalityId}/managers/{managerCode}")
    @ApiOperation(value = "Unassigned manager from municipality")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Unassigned manager", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> unassignedManagerFromMunicipality(@PathVariable Long municipalityId,
                                                               @PathVariable Long managerCode) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            workspaceBusiness.unassignedManagerFromMunicipality(municipalityId, managerCode);
            responseDto = new BasicResponseDto("Se ha desasignado el gestor del municipio", 7);
            httpStatus = HttpStatus.NO_CONTENT;

        } catch (BusinessException e) {
            log.error("Error WorkspaceV1Controller@unassignManagerFromMunicipality#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@unassignManagerFromMunicipality#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/operators", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get workspaces by operator")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Obtained workspaces", response = WorkspaceDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getWorkspacesByOperator(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get operator
            MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorByUserCode(userDtoSession.getId());
            if (operatorDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el operador.");
            }

            responseDto = workspaceBusiness.getWorkspacesByOperator(operatorDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error WorkspaceV1Controller@getWorkspacesByOperator#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getWorkspacesByOperator#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/operators/{operatorCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get workspaces by operator")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Obtained workspaces", response = WorkspaceDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> getWorkspacesByOperator(@PathVariable Long operatorCode) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            responseDto = workspaceBusiness.getWorkspacesByOperator(operatorCode);
            httpStatus = HttpStatus.OK;

        } catch (Exception e) {
            log.error("Error WorkspaceV1Controller@getWorkspacesByOperator#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
