package com.ai.st.microservice.workspaces.controllers.v2;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import com.ai.st.microservice.workspaces.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.business.ManagerBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.business.WorkspaceBusiness;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;
import com.google.common.io.Files;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Workspaces", tags = {"Workspaces"})
@RestController
@RequestMapping("api/workspaces/v2/workspaces")
public class WorkspaceV2Controller {

    private final Logger log = LoggerFactory.getLogger(WorkspaceV2Controller.class);

    @Autowired
    private WorkspaceBusiness workspaceBusiness;

    @Autowired
    private UserBusiness userBusiness;

    @Autowired
    private ManagerBusiness managerBusiness;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(value = "validate-municipalities-to-assign", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get municipalities where manager does not belong in")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Municipalities validated", response = ValidationMunicipalitiesDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> validateMunicipalitiesBeforeAssigned(
            @RequestParam(name = "municipalities") List<Long> municipalities) {

        HttpStatus httpStatus = null;
        Object responseDto = null;

        try {

            responseDto = workspaceBusiness.validateMunicipalitiesToAssign(municipalities);
            httpStatus = HttpStatus.OK;

        } catch (BusinessException e) {
            log.error(
                    "Error WorkspaceV2Controller@validateMunicipalitiesBeforeAssigned#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error(
                    "Error WorkspaceV2Controller@validateMunicipalitiesBeforeAssigned#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/assign-manager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Assign manager")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Manager assigned", response = WorkspaceDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> assignManager(@ModelAttribute AssignManagerDto assignManagerDto) {

        HttpStatus httpStatus = null;
        Object responseDto = null;

        try {

            // validation manager code
            Long managerCode = assignManagerDto.getManagerCode();
            if (managerCode == null || managerCode <= 0) {
                throw new InputValidationException("El gestor es requerido.");
            }

            // validation municipalities
            int countMunicipalities = assignManagerDto.getMunicipalities().size();
            if (countMunicipalities == 0) {
                throw new InputValidationException("Se debe seleccionar mínimo un municipio.");
            }

            // validation observations
            String observations = assignManagerDto.getObservations();
            if (observations == null || observations.isEmpty()) {
                throw new InputValidationException("Las observaciones son requeridas.");
            }

            // validation start date
            String startDateString = assignManagerDto.getStartDate();
            Date startDate = null;
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

            // validation support
            MultipartFile supportFile = assignManagerDto.getSupportFile();
            if (supportFile.isEmpty()) {
                throw new InputValidationException("El archivo de soporte es requerido.");
            }

            responseDto = workspaceBusiness.assignManager(startDate, managerCode, assignManagerDto.getMunicipalities(),
                    observations, supportFile);
            httpStatus = HttpStatus.CREATED;

        } catch (InputValidationException e) {
            log.error("Error WorkspaceV2Controller@assignManager#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV2Controller@assignManager#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV2Controller@assignManager#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "{workspaceId}/download-support-manager/{managerCode}", method = RequestMethod.GET)
    @ApiOperation(value = "Download file")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "File downloaded", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> downloadSupportManager(@PathVariable Long workspaceId, @PathVariable Long managerCode,
                                                    @RequestHeader("authorization") String headerAuthorization) {

        MediaType mediaType = null;
        File file = null;
        InputStreamResource resource = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            String pathFile = null;
            if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para descargar el soporte.");
                }

                pathFile = workspaceBusiness.getManagerSupportURL(workspaceId, managerCode, managerDto.getId());
            } else {
                pathFile = workspaceBusiness.getManagerSupportURL(workspaceId, managerCode, null);
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
            log.error("Error WorkspaceV2Controller@downloadSupportManager#Microservice ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 4), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV2Controller@downloadSupportManager#Business ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 2), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            log.error("Error WorkspaceV2Controller@downloadSupportManager#General ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 3), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType).contentLength(file.length())
                .header("extension", Files.getFileExtension(file.getName()))
                .header("filename", file.getName() + Files.getFileExtension(file.getName())).body(resource);

    }

    @RequestMapping(value = "/{workspaceId}/managers/{managerCode}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update manager from workspace")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Manager updated", response = WorkspaceDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> updateManagerFromWorkspace(
            @RequestBody UpdateManagerFromWorkspaceDto requestUpdateWorkspace, @PathVariable Long workspaceId,
            @PathVariable Long managerCode, @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus = null;
        Object responseDto = null;

        try {

            // validation observations
            String observations = requestUpdateWorkspace.getObservations();
            if (observations == null || observations.isEmpty()) {
                throw new InputValidationException("Las observaciones son requeridas.");
            }

            // validation start date
            String startDateString = requestUpdateWorkspace.getStartDate();
            Date startDate = null;
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

            responseDto = workspaceBusiness.updateManagerFromWorkspace(workspaceId, managerCode, startDate,
                    observations);
            httpStatus = HttpStatus.OK;

        } catch (InputValidationException e) {
            log.error("Error WorkspaceV2Controller@updateManagerFromWorkspace#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV2Controller@updateManagerFromWorkspace#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV2Controller@updateManagerFromWorkspace#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/{workspaceId}/operators/{operatorCode}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update operator from workspace")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Operator updated", response = WorkspaceDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> updateOperatorFromWorkspace(
            @ModelAttribute UpdateOperatorFromWorkspaceDto updateOperatorWorkspace, @PathVariable Long workspaceId,
            @PathVariable Long operatorCode, @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus = null;
        Object responseDto = null;

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
                throw new InputValidationException("El usuario no tiene permisos para actualizar el soporte.");
            }

            // validation start date
            String startDateString = updateOperatorWorkspace.getStartDate();
            Date startDate = null;
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
            String endDateString = updateOperatorWorkspace.getEndDate();
            Date endDate = null;
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

            // validation observations
            String observations = updateOperatorWorkspace.getObservations();
            if (observations == null || observations.isEmpty()) {
                throw new InputValidationException("Las observaciones son requeridas.");
            }

            // validation number parcels expected
            Long parcelsNumber = updateOperatorWorkspace.getNumberParcelsExpected();
            if (parcelsNumber != null) {
                if (parcelsNumber < 0) {
                    throw new InputValidationException("El número de predios es inválido.");
                }
            }

            // validation municipality area
            Double workArea = updateOperatorWorkspace.getWorkArea();
            if (workArea != null) {
                if (workArea < 0) {
                    throw new InputValidationException("El área es inválida.");
                }
            }

            responseDto = workspaceBusiness.updateOperatorFromWorkspace(workspaceId, managerDto.getId(), operatorCode, startDate, endDate,
                    observations, parcelsNumber, workArea, updateOperatorWorkspace.getSupportFile());
            httpStatus = HttpStatus.OK;

        } catch (InputValidationException e) {
            log.error("Error WorkspaceV2Controller@updateOperatorFromWorkspace#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV2Controller@updateOperatorFromWorkspace#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error WorkspaceV2Controller@updateOperatorFromWorkspace#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "{workspaceId}/download-support-operator/{operatorCode}", method = RequestMethod.GET)
    @ApiOperation(value = "Download file")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "File downloaded", response = BasicResponseDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> downloadSupportOperator(@PathVariable Long workspaceId, @PathVariable Long operatorCode,
                                                     @RequestHeader("authorization") String headerAuthorization) {

        MediaType mediaType = null;
        File file = null;
        InputStreamResource resource = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            String pathFile = null;
            if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }

                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para descargar el soporte.");
                }

                pathFile = workspaceBusiness.getOperatorSupportURL(workspaceId, operatorCode, managerDto.getId());
            } else {
                pathFile = workspaceBusiness.getOperatorSupportURL(workspaceId, operatorCode, null);
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
            log.error("Error WorkspaceV2Controller@downloadSupportOperator#Microservice ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 4), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (BusinessException e) {
            log.error("Error WorkspaceV2Controller@downloadSupportOperator#Business ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 2), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            log.error("Error WorkspaceV2Controller@downloadSupportOperator#General ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 3), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType).contentLength(file.length())
                .header("extension", Files.getFileExtension(file.getName()))
                .header("filename", file.getName() + Files.getFileExtension(file.getName())).body(resource);

    }
}
