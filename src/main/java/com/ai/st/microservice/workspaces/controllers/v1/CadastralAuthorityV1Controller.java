package com.ai.st.microservice.workspaces.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Cadastral Authority Processes ", tags = { "Cadastral Authority" })
@RestController
@RequestMapping("api/workspaces/v1/cadastral-authority")
public class CadastralAuthorityV1Controller {

	private final Logger log = LoggerFactory.getLogger(CadastralAuthorityV1Controller.class);

	@Autowired
	private CadastralAuthorityBusiness cadastralAuthorityBusiness;

	@Autowired
	private UserBusiness userBusiness;

	@RequestMapping(value = "/supplies/{municipalityId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create supply (cadastral authority)")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Supply created", response = MicroserviceSupplyDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createSupply(@PathVariable Long municipalityId,
			@RequestHeader("authorization") String headerAuthorization,
			@RequestParam(name = "file", required = false) MultipartFile file,
			@ModelAttribute CreateSupplyCadastralAuthorityDto supplyCadastralAuthorityDto) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// validation type supply
			Long attachmentTypeId = supplyCadastralAuthorityDto.getAttachmentTypeId();
			if (attachmentTypeId == null || attachmentTypeId <= 0) {
				throw new InputValidationException("El tipo de adjunto es inválido.");
			}

			// validation observations
			String observations = supplyCadastralAuthorityDto.getObservations();
			if (observations == null || observations.isEmpty()) {
				throw new InputValidationException("Las observaciones son requeridas.");
			}

			responseDto = cadastralAuthorityBusiness.createSupplyCadastralAuthority(municipalityId, attachmentTypeId,
					observations, supplyCadastralAuthorityDto.getFtp(), file, userDtoSession.getId());
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

}
