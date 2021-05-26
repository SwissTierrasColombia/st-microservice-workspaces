package com.ai.st.microservice.workspaces.controllers.v1;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import com.ai.st.microservice.workspaces.business.OperatorBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.business.WorkspaceOperatorBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.google.common.io.Files;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Operators", tags = {"Operators"})
@RestController
@RequestMapping("api/workspaces/v1/operators")
public class OperatorV1Controller {

    private final Logger log = LoggerFactory.getLogger(OperatorV1Controller.class);

    private final WorkspaceOperatorBusiness workspaceOperatorBusiness;
    private final ServletContext servletContext;
    private final UserBusiness userBusiness;
    private final OperatorBusiness operatorBusiness;

    public OperatorV1Controller(WorkspaceOperatorBusiness workspaceOperatorBusiness, ServletContext servletContext,
                                UserBusiness userBusiness, OperatorBusiness operatorBusiness) {
        this.workspaceOperatorBusiness = workspaceOperatorBusiness;
        this.servletContext = servletContext;
        this.userBusiness = userBusiness;
        this.operatorBusiness = operatorBusiness;
    }

    @RequestMapping(value = "/deliveries/{deliveryId}/disable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Disable delivery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Delivery disabled", response = MicroserviceDeliveryDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> disableDelivery(@PathVariable Long deliveryId,
                                                  @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get operator
            MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorByUserCode(userDtoSession.getId());
            if (operatorDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el operador.");
            }

            responseDto = workspaceOperatorBusiness.disableDelivery(operatorDto.getId(), deliveryId);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error OperatorV1Controller@disableDelivery#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error OperatorV1Controller@disableDelivery#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error OperatorV1Controller@disableDelivery#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/deliveries/{deliveryId}/reports-individual/{supplyId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Download report individual")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Download report individual"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> reportDownloadSupplyIndividual(@PathVariable Long deliveryId, @PathVariable Long supplyId,
                                                            @RequestHeader("authorization") String headerAuthorization) {

        MediaType mediaType;
        File file;
        InputStreamResource resource;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get operator
            MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorByUserCode(userDtoSession.getId());
            if (operatorDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el operador.");
            }

            String pathFile = workspaceOperatorBusiness.generateReportDownloadSupplyIndividual(operatorDto.getId(),
                    deliveryId, supplyId);

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
            log.error("Error OperatorV1Controller@reportDownloadSupplyIndividual#Microservice ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 4), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (BusinessException e) {
            log.error("Error OperatorV1Controller@reportDownloadSupplyIndividual#Business ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 2), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            log.error("Error OperatorV1Controller@reportDownloadSupplyIndividual#General ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 3), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType).contentLength(file.length())
                .header("extension", Files.getFileExtension(file.getName())).body(resource);
    }

    @RequestMapping(value = "/deliveries/{deliveryId}/reports-total", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Download report total")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Download report total"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> reportDownloadSupplyTotal(@PathVariable Long deliveryId,
                                                       @RequestHeader("authorization") String headerAuthorization) {

        MediaType mediaType;
        File file;
        InputStreamResource resource;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // get operator
            MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorByUserCode(userDtoSession.getId());
            if (operatorDto == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el operador.");
            }

            String pathFile = workspaceOperatorBusiness.generateReportDownloadSupplyTotal(operatorDto.getId(),
                    deliveryId);

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
            log.error("Error OperatorV1Controller@reportDownloadSupplyTotal#Microservice ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 4), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (BusinessException e) {
            log.error("Error OperatorV1Controller@reportDownloadSupplyTotal#Business ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 2), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            log.error("Error OperatorV1Controller@reportDownloadSupplyTotal#General ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 3), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType).contentLength(file.length())
                .header("extension", Files.getFileExtension(file.getName())).body(resource);
    }

}
