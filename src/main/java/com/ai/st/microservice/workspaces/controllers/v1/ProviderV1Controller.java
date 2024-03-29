package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.general.BasicResponseDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.*;
import com.ai.st.microservice.workspaces.dto.AnswerRequestDto;
import com.ai.st.microservice.workspaces.dto.CreateProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.CreateRequestDto;
import com.ai.st.microservice.workspaces.dto.CreateTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.TypeSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomRequestDto;

import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
import com.ai.st.microservice.workspaces.services.tracing.TracingKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(value = "Manage Providers", tags = { "Providers" })
@RestController
@RequestMapping("api/workspaces/v1/providers")
public class ProviderV1Controller {

    private final Logger log = LoggerFactory.getLogger(ProviderV1Controller.class);

    private final WorkspaceBusiness workspaceBusiness;
    private final ProviderBusiness providerBusiness;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final AdministrationBusiness administrationBusiness;

    public ProviderV1Controller(WorkspaceBusiness workspaceBusiness, ProviderBusiness providerBusiness,
            ManagerMicroserviceBusiness managerBusiness, AdministrationBusiness administrationBusiness) {
        this.workspaceBusiness = workspaceBusiness;
        this.providerBusiness = providerBusiness;
        this.managerBusiness = managerBusiness;
        this.administrationBusiness = administrationBusiness;
    }

    @PostMapping(value = "/municipalities/{municipalityId}/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create request")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Create request", response = CustomRequestDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> createRequest(@RequestBody CreateRequestDto createRequestDto,
            @RequestHeader("authorization") String headerAuthorization, @PathVariable Long municipalityId) {

        HttpStatus httpStatus;
        List<CustomRequestDto> listRequests = new ArrayList<>();
        Object responseDto = null;

        try {

            SCMTracing.setTransactionName("createSuppliesRequest");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);
            SCMTracing.addCustomParameter(TracingKeyword.BODY_REQUEST, createRequestDto.toString());

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
                throw new InputValidationException("El usuario no tiene permisos para actualizar el soporte.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.MANAGER_ID, managerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.MANAGER_NAME, managerDto.getName());

            // validation deadline
            String deadlineString = createRequestDto.getDeadline();
            Date deadline;
            if (deadlineString != null && !deadlineString.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    deadline = sdf.parse(deadlineString);
                } catch (Exception e) {
                    throw new InputValidationException("La fecha límite es inválida.");
                }
            } else {
                throw new InputValidationException("La fecha límite es requerida.");
            }

            // validation supplies
            List<TypeSupplyRequestedDto> supplies = createRequestDto.getSupplies();
            if (supplies.size() > 0) {
                for (TypeSupplyRequestedDto supplyDto : supplies) {
                    if (supplyDto.getTypeSupplyId() == null || supplyDto.getTypeSupplyId() <= 0) {
                        throw new InputValidationException("El tipo de insumo es inválido.");
                    }
                    if (supplyDto.getProviderId() == null || supplyDto.getProviderId() <= 0) {
                        throw new InputValidationException("El proveedor de insumo es inválido.");
                    }
                }
            } else {
                throw new InputValidationException(
                        "La solicitud debe contener al menos un tipo de insumo a solicitar.");
            }

            listRequests = workspaceBusiness.createRequest(deadline, supplies, userDtoSession.getId(),
                    managerDto.getId(), municipalityId);
            httpStatus = HttpStatus.CREATED;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@createRequest#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (InputValidationException e) {
            log.error("Error ProviderV1Controller@createRequest#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@createRequest#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@createRequest#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
                : new ResponseEntity<>(listRequests, httpStatus);
    }

    @GetMapping(value = "/pending-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get pending requests by provider")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get pending requests by provider", response = CustomRequestDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getRequestsPendingByProveedor(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        List<CustomRequestDto> listRequests = new ArrayList<>();
        Object responseDto = null;

        try {

            SCMTracing.setTransactionName("getRequestsPendingByProveedor");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserTechnical(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            listRequests = workspaceBusiness.getPendingRequestByProvider(userDtoSession.getId(), providerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@getRequestsPendingByProveedor#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@getRequestsPendingByProveedor#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@getRequestsPendingByProveedor#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
                : new ResponseEntity<>(listRequests, httpStatus);
    }

    @GetMapping(value = "/closed-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get closed-requests")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get closed requests by provider", response = CustomRequestDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getRequestsClosedByProveedor(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        List<CustomRequestDto> listRequests = new ArrayList<>();
        Object responseDto = null;

        try {

            SCMTracing.setTransactionName("getRequestsClosedByProveedor");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserTechnicalOrAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            listRequests = workspaceBusiness.getClosedRequestByProvider(userDtoSession.getId(), providerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@getRequestsClosedByProveedor#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@getRequestsClosedByProveedor#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@getRequestsClosedByProveedor#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
                : new ResponseEntity<>(listRequests, httpStatus);
    }

    @PutMapping(value = "/requests/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Answer request")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Answer request", response = CustomRequestDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> answerRequest(@PathVariable Long requestId,
            @RequestHeader("authorization") String headerAuthorization,
            @RequestParam(name = "files[]", required = false) MultipartFile[] files,
            @RequestParam(name = "extra", required = false) MultipartFile extraFile,
            @ModelAttribute AnswerRequestDto answerRequest) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("answerSuppliesRequest");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);
            SCMTracing.addCustomParameter(TracingKeyword.BODY_REQUEST, answerRequest.toString());

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserTechnical(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            // validation type supply
            Long typeSupplyId = answerRequest.getTypeSupplyId();
            if (typeSupplyId == null || typeSupplyId <= 0) {
                throw new InputValidationException("El tipo de insumo es inválido.");
            }

            responseDto = providerBusiness.answerRequest(requestId, typeSupplyId, answerRequest.getSkipErrors(),
                    answerRequest.getJustification(), files, extraFile, answerRequest.getUrl(), providerDto,
                    userDtoSession.getId(), answerRequest.getObservations());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@answerRequest#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (InputValidationException e) {
            log.error("Error ProviderV1Controller@answerRequest#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@answerRequest#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@answerRequest#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PutMapping(value = "/requests/{requestId}/close", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Close request")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Close request", response = CustomRequestDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> closeRequest(@PathVariable Long requestId,
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("closeRequest");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserTechnical(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.closeRequest(requestId, providerDto, userDtoSession.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@closeRequest#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@closeRequest#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@closeRequest#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/requests/emmiters", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Close request")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Close request", response = CustomRequestDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getRequestsByEmitters(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getRequestsByEmitters");
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

            responseDto = providerBusiness.getRequestsByEmittersManager(managerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@getRequestsByEmmiters#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@getRequestsByEmmiters#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@getRequestsByEmmiters#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PostMapping(value = "/types-supplies", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create type supply")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Create type supply", response = MicroserviceTypeSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<Object> createTypeSupply(@RequestBody CreateTypeSupplyDto createTypeSupplyDto,
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("createTypeSupply");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);
            SCMTracing.addCustomParameter(TracingKeyword.BODY_REQUEST, createTypeSupplyDto.toString());

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para crear insumos.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.createTypeSupply(providerDto.getId(), createTypeSupplyDto.getName(),
                    createTypeSupplyDto.getDescription(), createTypeSupplyDto.getMetadataRequired(),
                    createTypeSupplyDto.getModelRequired(), createTypeSupplyDto.getProviderProfileId(),
                    createTypeSupplyDto.getExtensions());
            httpStatus = HttpStatus.CREATED;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@createTypeSupply#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@createTypeSupply#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@createTypeSupply#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/types-supplies", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get types supplies")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get types supplies", response = MicroserviceTypeSupplyDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getTypeSupplies(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getTypeSupplies");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para consultar insumos.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.getTypesSuppliesByProvider(providerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@getTypeSupplies#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@getTypeSupplies#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@getTypeSupplies#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PutMapping(value = "/types-supplies/{typeSupplyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update type supply")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Update type supply", response = MicroserviceTypeSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> updateTypeSupply(@PathVariable Long typeSupplyId,
            @RequestBody CreateTypeSupplyDto updateTypeSupplyDto,
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("updateTypeSupply");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);
            SCMTracing.addCustomParameter(TracingKeyword.BODY_REQUEST, updateTypeSupplyDto.toString());

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new InputValidationException("El usuario no tiene permisos para modificar insumos.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.updateTypeSupply(providerDto.getId(), typeSupplyId,
                    updateTypeSupplyDto.getName(), updateTypeSupplyDto.getDescription(),
                    updateTypeSupplyDto.getMetadataRequired(), updateTypeSupplyDto.getModelRequired(),
                    updateTypeSupplyDto.getProviderProfileId(), updateTypeSupplyDto.getExtensions());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@updateTypeSupply#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@updateTypeSupply#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@updateTypeSupply	#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PutMapping(value = "/types-supplies/{typeSupplyId}/enable", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Enable type supply")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Type supply enabled", response = MicroserviceTypeSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> enableTypeSupply(@PathVariable Long typeSupplyId,
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("enableTypeSupply");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new BusinessException("No tiene permiso para modificar el tipo de insumo.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.enableTypeSupply(providerDto.getId(), typeSupplyId);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@enableTypeSupply#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@enableTypeSupply#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@enableTypeSupply	#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PutMapping(value = "/types-supplies/{typeSupplyId}/disable", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Disable type supply")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Type supply disabled", response = MicroserviceTypeSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> disableTypeSupply(@PathVariable Long typeSupplyId,
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("disableTypeSupply");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new BusinessException("No tiene permiso para modificar el tipo de insumo.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.disableTypeSupply(providerDto.getId(), typeSupplyId);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@disableTypeSupply#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@disableTypeSupply#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@disableTypeSupply	#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @DeleteMapping(value = "/types-supplies/{typeSupplyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete type supply")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Delete type supply"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> deleteTypeSupply(@PathVariable Long typeSupplyId,
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            SCMTracing.setTransactionName("deleteTypeSupply");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new BusinessException("No tiene permiso para eliminar insumos.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            providerBusiness.deleteTypeSupply(providerDto.getId(), typeSupplyId);
            httpStatus = HttpStatus.NO_CONTENT;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@deleteTypeSupply#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@deleteTypeSupply#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@deleteTypeSupply	#General ---> " + e.getMessage());
            responseDto = new BasicResponseDto(e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PostMapping(value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create profile")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Profile created", response = MicroserviceProviderProfileDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> createProviderProfile(@RequestBody CreateProviderProfileDto createProfileDto,
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("createProviderProfile");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);
            SCMTracing.addCustomParameter(TracingKeyword.BODY_REQUEST, createProfileDto.toString());

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new BusinessException("No tiene permiso para crear perfiles.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.createProfile(providerDto.getId(), createProfileDto.getName(),
                    createProfileDto.getDescription());

            httpStatus = HttpStatus.CREATED;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@createProviderProfile#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@createProviderProfile#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@createProviderProfile#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get profiles")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get profiles", response = MicroserviceProviderProfileDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getProviderProfiles(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getProviderProfiles");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new BusinessException("No tiene permiso para consultar perfiles.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.getProfilesByProvider(providerDto.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@getProviderProfiles#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@getProviderProfiles#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@getProviderProfiles#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PutMapping(value = "/profiles/{profileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Update profile", response = MicroserviceProviderProfileDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> updateProviderProfile(@PathVariable Long profileId,
            @RequestHeader("authorization") String headerAuthorization,
            @RequestBody CreateProviderProfileDto updateProfileDto) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("updateProviderProfile");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);
            SCMTracing.addCustomParameter(TracingKeyword.BODY_REQUEST, updateProfileDto.toString());

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new BusinessException("No tiene permiso para modificar perfiles.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            responseDto = providerBusiness.updateProfile(providerDto.getId(), profileId, updateProfileDto.getName(),
                    updateProfileDto.getDescription());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@updateProviderProfile#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@updateProviderProfile#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@updateProviderProfile#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @DeleteMapping(value = "/profiles/{profileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete profile")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Delete profile"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> deleteProviderProfile(@PathVariable Long profileId,
            @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            SCMTracing.setTransactionName("deleteProviderProfile");
            SCMTracing.addCustomParameter(TracingKeyword.AUTHORIZATION_HEADER, headerAuthorization);

            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }
            SCMTracing.addCustomParameter(TracingKeyword.USER_ID, userDtoSession.getId());
            SCMTracing.addCustomParameter(TracingKeyword.USER_EMAIL, userDtoSession.getEmail());
            SCMTracing.addCustomParameter(TracingKeyword.USER_NAME, userDtoSession.getUsername());

            MicroserviceProviderDto providerDto = providerBusiness
                    .getProviderByUserAdministrator(userDtoSession.getId());
            if (providerDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
            }
            if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                throw new BusinessException("No tiene permiso para remover perfiles.");
            }
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_ID, providerDto.getId());
            SCMTracing.addCustomParameter(TracingKeyword.PROVIDER_NAME, providerDto.getName());

            providerBusiness.deleteProfile(providerDto.getId(), profileId);
            httpStatus = HttpStatus.NO_CONTENT;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@deleteProviderProfile#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@deleteProviderProfile#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@deleteProviderProfile#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/requests/municipality", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get requests by municipality")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Requests got", response = CustomRequestDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getRequestsByMunicipalityForManager(
            @RequestHeader("authorization") String headerAuthorization, @RequestParam(name = "page") Integer page,
            @RequestParam(name = "municipality") String municipalityCode) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getRequestsByMunicipalityForManager");
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

            responseDto = providerBusiness.getRequestsByManagerAndMunicipality(page, managerDto.getId(),
                    municipalityCode);

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@getRequestsByMunicipalityForManager#Microservice ---> "
                    + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@getRequestsByMunicipalityForManager#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@getRequestsByMunicipalityForManager#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/requests/provider", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get requests by provider")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get requests", response = CustomRequestDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getRequestsByProviderForManager(@RequestHeader("authorization") String headerAuthorization,
            @RequestParam(name = "page") Integer page, @RequestParam(name = "provider") Long providerId) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getRequestsByProviderForManager");
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

            responseDto = providerBusiness.getRequestsByManagerAndProvider(page, managerDto.getId(), providerId);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@getRequestsByProviderForManager#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@getRequestsByProviderForManager#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@getRequestsByProviderForManager#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping(value = "/requests/package", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get requests by package")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get requests", response = CustomRequestDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class) })
    @ResponseBody
    public ResponseEntity<?> getRequestsByPackageForManager(@RequestHeader("authorization") String headerAuthorization,
            @RequestParam(name = "package", required = false) String packageLabel) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            SCMTracing.setTransactionName("getRequestsByPackageForManager");
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

            responseDto = providerBusiness.getRequestsByManagerAndPackage(managerDto.getId(), packageLabel);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error ProviderV1Controller@getRequestsByPackageForManager#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (BusinessException e) {
            log.error("Error ProviderV1Controller@getRequestsByPackageForManager#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        } catch (Exception e) {
            log.error("Error ProviderV1Controller@getRequestsByPackageForManager#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage());
            SCMTracing.sendError(e.getMessage());
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
