package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.general.BasicResponseDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.CadastralAuthorityBusiness;
import com.ai.st.microservice.workspaces.dto.CreateSupplyCadastralAuthorityDto;
import com.ai.st.microservice.workspaces.dto.supplies.CustomSupplyDto;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Cadastral Authority Processes ", tags = {"Cadastral Authority"})
@RestController
@RequestMapping("api/workspaces/v1/cadastral-authority")
public class CadastralAuthorityV1Controller {

    private final Logger log = LoggerFactory.getLogger(CadastralAuthorityV1Controller.class);

    private final CadastralAuthorityBusiness cadastralAuthorityBusiness;
    private final ServletContext servletContext;
    private final AdministrationBusiness administrationBusiness;

    public CadastralAuthorityV1Controller(CadastralAuthorityBusiness cadastralAuthorityBusiness, ServletContext servletContext,
                                          AdministrationBusiness administrationBusiness) {
        this.cadastralAuthorityBusiness = cadastralAuthorityBusiness;
        this.servletContext = servletContext;
        this.administrationBusiness = administrationBusiness;
    }

    @PostMapping(value = "/supplies/{municipalityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create supply (cadastral authority)")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Supply created", response = CustomSupplyDto.class),
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
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
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

    @GetMapping(value = "/report/{municipalityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create report (cadastral authority)")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Supply created", response = CustomSupplyDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> downloadReport(@PathVariable Long municipalityId,
                                                 @RequestParam(name = "manager") Long managerCode) {

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
