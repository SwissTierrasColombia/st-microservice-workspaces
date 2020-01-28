package com.ai.st.microservice.workspaces.business;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.dto.providers.MicroserivceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.RabbitMQSenderService;

@Component
public class ProviderBusiness {

	@Value("${st.temporalDirectory}")
	private String stTemporalDirectory;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private RabbitMQSenderService rabbitMQService;

	@Autowired
	private SupplyBusiness supplyBusiness;

	public MicroserviceRequestDto answerRequest(Long requestId, Long typeSupplyId, String justification,
			MultipartFile[] files, String url, MicroserviceProviderDto providerDto, Long userCode, String observations)
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

		Boolean delivered = (files.length > 0 || (url != null && !url.isEmpty())) ? true : false;
		if (delivered && (observations == null || observations.isEmpty())) {
			throw new BusinessException("Las observaciones son requeridas.");
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

				if (delivered == true) {

					// send supply to microservice supplies
					try {
						List<String> urls = new ArrayList<String>();
						if (files.length > 0) {
							for (MultipartFile file : files) {
								// save file with microservice file manager
								String urlDocumentaryRepository = null;
								try {

									String tmpFile = this.stTemporalDirectory + File.separatorChar
											+ StringUtils.cleanPath(file.getOriginalFilename());

									FileUtils.writeByteArrayToFile(new File(tmpFile), file.getBytes());

									String urlBase = "/" + requestDto.getMunicipalityCode().replace(" ", "_")
											+ "/insumos/proveedores/" + providerDto.getName().replace(" ", "_") + "/"
											+ supplyRequested.getTypeSupply().getName().replace(" ", "_");

									urlDocumentaryRepository = rabbitMQService.sendFile(
											StringUtils.cleanPath(file.getOriginalFilename()), urlBase, "Local");

									if (urlDocumentaryRepository == null) {
										throw new BusinessException(
												"No se ha podido guardar el archivo en el repositorio documental.");
									}
									urls.add(urlDocumentaryRepository);
								} catch (IOException e) {
									throw new BusinessException("No se ha podido cargar el soporte.");
								}
							}
						}

						supplyBusiness.createSupply(requestDto.getMunicipalityCode(), observations, typeSupplyId, urls,
								url, userCode, providerDto.getId(), null);

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
