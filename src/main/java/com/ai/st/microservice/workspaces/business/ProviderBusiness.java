package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.FilemanagerFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.dto.filemanager.MicroserviceFilemanagerResponseDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserivceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyOwnerDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class ProviderBusiness {

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private SupplyFeignClient supplyClient;

	@Autowired
	private FilemanagerFeignClient filemanagerClient;

	public MicroserviceRequestDto answerRequest(Long requestId, Long typeSupplyId, String justification,
			MultipartFile[] files, String url, MicroserviceProviderDto providerDto, Long userCode)
			throws BusinessException {

		MicroserviceRequestDto requestUpdatedDto = null;

		if (files.length == 0 && (url == null || url.isEmpty()) && (justification == null || justification.isEmpty())) {
			throw new BusinessException("Se debe justificar porque no se cargará el insumo.");
		}

		MicroserviceRequestDto requestDto = providerClient.findRequestById(requestId);

		if (providerDto.getId() != requestDto.getProvider().getId()) {
			throw new BusinessException("No tiene acceso a la solicitud.");
		}

		if (requestDto.getRequestState().getId() != 1) {
			throw new BusinessException("La solicitud esta cerrada, no se puede modificar.");
		}

		List<MicroserviceProviderUserDto> usersByProvider = providerClient.findUsersByProviderId(providerDto.getId());
		MicroserviceProviderUserDto userProviderFound = usersByProvider.stream()
				.filter(user -> userCode == user.getUserCode()).findAny().orElse(null);
		if (userProviderFound == null) {
			throw new BusinessException("El usuario no esta registrado como usuario para el proveedor de insumo.");
		}

		Boolean searchSupply = false;
		for (MicroserivceSupplyRequestedDto supplyRequested : requestDto.getSuppliesRequested()) {

			if (supplyRequested.getTypeSupply().getId() == typeSupplyId) {

				// verify if the user's profile matches the input profile
				MicroserviceProviderProfileDto profileSupply = supplyRequested.getTypeSupply().getProviderProfile();
				MicroserviceProviderProfileDto profileUser = userProviderFound.getProfiles().stream()
						.filter(profile -> profileSupply.getId() == profile.getId()).findAny().orElse(null);
				if (profileUser == null) {
					throw new BusinessException(
							"El usuario no tiene asignado el perfil necesario para cargar el tipo de insumo.");
				}

				// TODO: send supply to microservice supplies
				Boolean delivered = (files.length > 0 || (url != null && !url.isEmpty())) ? true : false;
				
				if (delivered == true) {
					try {
						MicroserviceCreateSupplyDto createSupplyDto = new MicroserviceCreateSupplyDto();
						createSupplyDto.setMunicipalityCode(requestDto.getMunicipalityCode());
						createSupplyDto.setObservations("N/A");
						createSupplyDto.setTypeSupplyCode(typeSupplyId);
						if (files.length > 0) {
							List<String> urls = new ArrayList<String>();
							for (MultipartFile file : files) {

								try {

									String urlBase = "/" + requestDto.getMunicipalityCode().replace(" ", "_")
											+ "/insumos/proveedores/" + providerDto.getName().replace(" ", "_") + "/"
											+ supplyRequested.getTypeSupply().getName().replace(" ", "_");

									MicroserviceFilemanagerResponseDto responseFilemanagerDto = filemanagerClient.saveFile(
											file.getBytes(), StringUtils.cleanPath(file.getOriginalFilename()), urlBase,
											"Local");

									if (!responseFilemanagerDto.getStatus()) {
										throw new BusinessException(
												"No se ha podido guardar el archivo en el repositorio documental.");
									}

									urls.add(responseFilemanagerDto.getUrl());
								} catch (Exception e) {
									throw new BusinessException(
											"No se ha podido guardar el archivo en el repositorio documental.");
								}
							}
							createSupplyDto.setUrlsDocumentaryRepository(urls);
						}
						if (url != null && !url.isEmpty()) {
							createSupplyDto.setUrl(url);
						}
						List<MicroserviceCreateSupplyOwnerDto> owners = new ArrayList<MicroserviceCreateSupplyOwnerDto>();

						MicroserviceCreateSupplyOwnerDto owner1 = new MicroserviceCreateSupplyOwnerDto();
						owner1.setOwnerCode(userCode);
						owner1.setOwnerType("USER");
						owners.add(owner1);

						MicroserviceCreateSupplyOwnerDto owner2 = new MicroserviceCreateSupplyOwnerDto();
						owner2.setOwnerCode(providerDto.getId());
						owner2.setOwnerType("ENTITY");
						owners.add(owner2);

						createSupplyDto.setOwners(owners);

						supplyClient.createSupply(createSupplyDto);
					} catch (Exception e) {
						throw new BusinessException("No se ha podido cargar el insumo.");
					}		
				}

				// Update request
				try {
					MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();
					updateSupply.setDelivered(delivered);
					updateSupply.setJustification(justification);
					requestUpdatedDto = providerClient.updateSupplyRequested(requestId, supplyRequested.getId(),
							updateSupply);
				} catch (Exception e) {
					throw new BusinessException("No se ha podido actualizar la información de la solicitud.");
				}

				searchSupply = true;
				break;
			}

		}
		if (!searchSupply) {
			throw new BusinessException("El tipo de insumo no pertenece a la solicitud.");
		}

		return requestUpdatedDto;
	}

	public MicroserviceRequestDto closeRequest(Long requestId, MicroserviceProviderDto providerDto, Long userCode)
			throws BusinessException {

		MicroserviceRequestDto requestDto = providerClient.findRequestById(requestId);

		if (providerDto.getId() != requestDto.getProvider().getId()) {
			throw new BusinessException("No tiene acceso a la solicitud.");
		}

		if (requestDto.getRequestState().getId() != 1) {
			throw new BusinessException("La solicitud esta cerrada, no se puede cerrar.");
		}

		List<MicroserviceProviderUserDto> usersByProvider = providerClient.findUsersByProviderId(providerDto.getId());
		MicroserviceProviderUserDto userProviderFound = usersByProvider.stream()
				.filter(user -> userCode == user.getUserCode()).findAny().orElse(null);
		if (userProviderFound == null) {
			throw new BusinessException("El usuario no esta registrado como usuario para el proveedor de insumo.");
		}

		for (MicroserivceSupplyRequestedDto supplyRequested : requestDto.getSuppliesRequested()) {
			if (!supplyRequested.getDelivered()
					&& (supplyRequested.getJustification() == null || supplyRequested.getJustification().isEmpty())) {
				throw new BusinessException(
						"No se puede cerrar la solicitud porque no se han cargado todos los insumos.");
			}
		}

		MicroserviceRequestDto requestUpdatedDto = null;

		try {
			requestUpdatedDto = providerClient.closeRequest(requestId);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido actualizar la información de la solicitud.");
		}

		return requestUpdatedDto;
	}

}
