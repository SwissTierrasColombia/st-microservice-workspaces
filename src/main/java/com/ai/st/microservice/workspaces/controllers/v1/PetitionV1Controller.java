package com.ai.st.microservice.workspaces.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.business.ManagerBusiness;
import com.ai.st.microservice.workspaces.business.ProviderBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.CreatePetitionDto;
import com.ai.st.microservice.workspaces.dto.UpdatePetitionDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroservicePetitionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Petitions", tags = { "Petitions" })
@RestController
@RequestMapping("api/workspaces/v1/petitions")
public class PetitionV1Controller {

	private final Logger log = LoggerFactory.getLogger(PetitionV1Controller.class);

	@Autowired
	private UserBusiness userBusiness;

	@Autowired
	private ManagerBusiness managerBusiness;

	@Autowired
	private ProviderBusiness providerBusiness;

	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Create petition")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Petition created", response = MicroservicePetitionDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> createPetition(@RequestBody CreatePetitionDto requestCreatePetition,
			@RequestHeader("authorization") String headerAuthorization) {

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
				throw new InputValidationException("El usuario no tiene permisos para crear peticiones.");
			}

			// validate description
			if (requestCreatePetition.getDescription() == null || requestCreatePetition.getDescription().isEmpty()) {
				throw new InputValidationException("La descripción es requerida");
			}

			// validate provider
			if (requestCreatePetition.getProviderId() == null || requestCreatePetition.getProviderId() <= 0) {
				throw new InputValidationException("El proveedor de insumo es requerido");
			}

			responseDto = providerBusiness.createPetition(requestCreatePetition.getProviderId(), managerDto.getId(),
					requestCreatePetition.getDescription());
			httpStatus = HttpStatus.CREATED;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error PetitionV1Controller@createPetition#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (InputValidationException e) {
			log.error("Error PetitionV1Controller@createPetition#Validator ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (BusinessException e) {
			log.error("Error PetitionV1Controller@createPetition#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 5);
		} catch (Exception e) {
			log.error("Error PetitionV1Controller@createPetition#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/manager/{providerId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get petitions for manager")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Petitions getted", response = MicroservicePetitionDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getPetitionsForManager(@RequestHeader("authorization") String headerAuthorization,
			@PathVariable Long providerId) {

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
				throw new InputValidationException("El usuario no tiene permisos para crear peticiones.");
			}

			responseDto = providerBusiness.getPetitionsForManager(providerId, managerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error PetitionV1Controller@getPetitionsForManager#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error PetitionV1Controller@getPetitionsForManager#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 5);
		} catch (Exception e) {
			log.error("Error PetitionV1Controller@getPetitionsForManager#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/provider/open", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get petitions for provider (open)")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Petitions getted", response = MicroservicePetitionDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getPetitionsForProviderOpen(
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para consultar esta información.");
			}

			responseDto = providerBusiness.getPetitionsForProviderOpen(providerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error PetitionV1Controller@getPetitionsForProviderOpen#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error PetitionV1Controller@getPetitionsForProviderOpen#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 5);
		} catch (Exception e) {
			log.error("Error PetitionV1Controller@getPetitionsForProviderOpen#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/provider/close", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get petitions for provider (close)")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Petitions getted", response = MicroservicePetitionDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> getPetitionsForProviderClose(
			@RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para consultar esta información.");
			}

			responseDto = providerBusiness.getPetitionsForProviderClose(providerDto.getId());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error PetitionV1Controller@getPetitionsForProviderClose#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (BusinessException e) {
			log.error("Error PetitionV1Controller@getPetitionsForProviderClose#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 5);
		} catch (Exception e) {
			log.error("Error PetitionV1Controller@getPetitionsForProviderClose#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/{petitionId}/accept", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Update petition")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Petition updated", response = MicroservicePetitionDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> acceptPetition(@RequestBody UpdatePetitionDto requestUpdatePetition,
			@PathVariable Long petitionId, @RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para realizar cambios en la petición.");
			}

			// validate description
			if (requestUpdatePetition.getJustification() == null
					|| requestUpdatePetition.getJustification().isEmpty()) {
				throw new InputValidationException("La justificación es requerida");
			}

			responseDto = providerBusiness.acceptPetition(providerDto.getId(), petitionId,
					requestUpdatePetition.getJustification());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error PetitionV1Controller@acceptPetition#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (InputValidationException e) {
			log.error("Error PetitionV1Controller@acceptPetition#Validator ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (BusinessException e) {
			log.error("Error PetitionV1Controller@acceptPetition#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 5);
		} catch (Exception e) {
			log.error("Error PetitionV1Controller@acceptPetition#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

	@RequestMapping(value = "/{petitionId}/reject", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Update petition")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Petition updated", response = MicroservicePetitionDto.class),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<Object> rejectPetition(@RequestBody UpdatePetitionDto requestUpdatePetition,
			@PathVariable Long petitionId, @RequestHeader("authorization") String headerAuthorization) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
			if (userDtoSession == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
			}

			// get provider
			MicroserviceProviderDto providerDto = providerBusiness
					.getProviderByUserAdministrator(userDtoSession.getId());
			if (providerDto == null) {
				throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
			}
			if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
				throw new BusinessException("No tiene permiso para realizar cambios en la petición.");
			}

			// validate description
			if (requestUpdatePetition.getJustification() == null
					|| requestUpdatePetition.getJustification().isEmpty()) {
				throw new InputValidationException("La justificación es requerida");
			}

			responseDto = providerBusiness.rejectPetition(providerDto.getId(), petitionId,
					requestUpdatePetition.getJustification());
			httpStatus = HttpStatus.OK;

		} catch (DisconnectedMicroserviceException e) {
			log.error("Error PetitionV1Controller@rejectPetition#Microservice ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 4);
		} catch (InputValidationException e) {
			log.error("Error PetitionV1Controller@rejectPetition#Validator ---> " + e.getMessage());
			httpStatus = HttpStatus.BAD_REQUEST;
			responseDto = new BasicResponseDto(e.getMessage(), 2);
		} catch (BusinessException e) {
			log.error("Error PetitionV1Controller@rejectPetition#Business ---> " + e.getMessage());
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			responseDto = new BasicResponseDto(e.getMessage(), 5);
		} catch (Exception e) {
			log.error("Error PetitionV1Controller@rejectPetition#General ---> " + e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseDto = new BasicResponseDto(e.getMessage(), 3);
		}

		return new ResponseEntity<>(responseDto, httpStatus);
	}

}
