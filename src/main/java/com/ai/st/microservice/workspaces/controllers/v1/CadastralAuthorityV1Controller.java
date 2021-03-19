package com.ai.st.microservice.workspaces.controllers.v1;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.business.CadastralAuthorityBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.CreateSupplyCadastralAuthorityDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;
import com.google.common.io.Files;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Cadastral Authority Processes ", tags = {"Cadastral Authority"})
@RestController
@RequestMapping("api/workspaces/v1/cadastral-authority")
public class CadastralAuthorityV1Controller {

    private final Logger log = LoggerFactory.getLogger(CadastralAuthorityV1Controller.class);

    @Autowired
    private CadastralAuthorityBusiness cadastralAuthorityBusiness;

    @Autowired
    private UserBusiness userBusiness;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(value = "/supplies/{municipalityId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create supply (cadastral authority)")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Supply created", response = MicroserviceSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> createSupply(@PathVariable Long municipalityId,
                                               @RequestHeader("authorization") String headerAuthorization,
                                               @RequestParam(name = "file", required = false) MultipartFile file,
                                               @ModelAttribute CreateSupplyCadastralAuthorityDto supplyCadastralAuthorityDto) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            // validation manager
            Long managerCode = supplyCadastralAuthorityDto.getManagerCode();
            if (managerCode == null || managerCode <= 0) {
                throw new InputValidationException("El gestor es requerido.");
            }

            // validation type supply
            Long attachmentTypeId = supplyCadastralAuthorityDto.getAttachmentTypeId();
            if (attachmentTypeId == null || attachmentTypeId <= 0) {
                throw new InputValidationException("El tipo de adjunto es inválido.");
            }

            // validation name
            String name = supplyCadastralAuthorityDto.getName();
            if (name == null || name.isEmpty()) {
                throw new InputValidationException("El nombre es requerido.");
            }

            // validation observations
            String observations = supplyCadastralAuthorityDto.getObservations();
            if (observations == null || observations.isEmpty()) {
                throw new InputValidationException("Las observaciones son requeridas.");
            }

            responseDto = cadastralAuthorityBusiness.createSupplyCadastralAuthority(municipalityId, managerCode, attachmentTypeId,
                    name, observations, supplyCadastralAuthorityDto.getFtp(), file, userDtoSession.getId());
            httpStatus = HttpStatus.CREATED;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error CadastralAuthorityV1Controller@createSupply#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error CadastralAuthorityV1Controller@createSupply#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error CadastralAuthorityV1Controller@createSupply#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error CadastralAuthorityV1Controller@createSupply#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/report/{municipalityId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create report (cadastral authority)")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Supply created", response = MicroserviceSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> downloadReport(@PathVariable Long municipalityId,
                                                 @RequestParam(name = "manager", required = true) Long managerCode) {

        MediaType mediaType;
        File file;
        InputStreamResource resource;

        try {

            String pathFile = cadastralAuthorityBusiness.generateReport(municipalityId, managerCode);

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

        } catch (BusinessException e) {
            log.error("Error CadastralAuthorityV1Controller@downloadReport#Business ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 2), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            log.error("Error CadastralAuthorityV1Controller@downloadReport#General ---> " + e.getMessage());
            return new ResponseEntity<>(new BasicResponseDto(e.getMessage(), 3), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType).contentLength(file.length())
                .header("extension", Files.getFileExtension(file.getName())).body(resource);

    }

}
