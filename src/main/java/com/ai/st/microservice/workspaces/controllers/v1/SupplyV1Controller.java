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

import com.ai.st.microservice.workspaces.business.RoleBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceRoleDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;

import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Supplies", description = "Manage Supplies", tags = { "Supplies" })
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

	@RequestMapping(value = "/{municipalityId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Get supplies by municipality")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get supplies", response = MicroserviceSupplyDto.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Error Server", response = String.class) })
	@ResponseBody
	public ResponseEntity<?> getSuppliesByMunicipality(@PathVariable Long municipalityId,
			@RequestParam(name = "extensions", required = false) List<String> extensions,
			@RequestHeader("authorization") String headerAuthorization,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "requests", required = false) List<Long> requests) {

		HttpStatus httpStatus = null;
		Object responseDto = null;

		try {

			// user session
			String token = headerAuthorization.replace("Bearer ", "").trim();
			MicroserviceUserDto userDtoSession = null;
			try {
				userDtoSession = userClient.findByToken(token);
			} catch (FeignException e) {
				throw new DisconnectedMicroserviceException(
						"No se ha podido establecer conexión con el microservicio de usuarios.");
			}

			MicroserviceRoleDto roleAdministrator = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_ADMINISTRATOR).findAny().orElse(null);

			MicroserviceRoleDto roleManager = userDtoSession.getRoles().stream()
					.filter(roleDto -> roleDto.getId() == RoleBusiness.ROLE_MANAGER).findAny().orElse(null);

			if (roleAdministrator instanceof MicroserviceRoleDto) {

				responseDto = supplyBusiness.getSuppliesByMunicipalityAdmin(municipalityId, extensions, page, requests);

			} else if (roleManager instanceof MicroserviceRoleDto) {

				// get manager
				MicroserviceManagerDto managerDto = null;
				try {
					managerDto = managerClient.findByUserCode(userDtoSession.getId());
				} catch (FeignException e) {
					throw new DisconnectedMicroserviceException(
							"No se ha podido establecer conexión con el microservicio de gestores.");
				}

				responseDto = supplyBusiness.getSuppliesByMunicipalityManager(municipalityId, managerDto.getId(),
						extensions, page, requests);
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

}
