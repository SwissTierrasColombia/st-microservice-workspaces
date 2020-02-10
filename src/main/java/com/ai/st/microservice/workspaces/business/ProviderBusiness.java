package com.ai.st.microservice.workspaces.business;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.TaskFeignClient;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceExtensionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCancelTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMemberDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataPropertyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.RabbitMQSenderService;

@Component
public class ProviderBusiness {

	public static final Long SUPPLY_REQUESTED_STATE_ACCEPTED = (long) 1;
	public static final Long SUPPLY_REQUESTED_STATE_REJECTED = (long) 2;
	public static final Long SUPPLY_REQUESTED_STATE_VALIDATING = (long) 3;
	public static final Long SUPPLY_REQUESTED_STATE_PENDING = (long) 4;
	public static final Long SUPPLY_REQUESTED_STATE_UNDELIVERED = (long) 5;

	public static final Long PROVIDER_IGAC_ID = (long) 1;

	public static final Long PROVIDER_PROFILE_CADASTRAL = (long) 1;

	public static final Long PROVIDER_SUPPLY_CADASTRAL = (long) 2;

	private final Logger log = LoggerFactory.getLogger(ProviderBusiness.class);

	@Value("${st.temporalDirectory}")
	private String stTemporalDirectory;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private TaskFeignClient taskClient;

	@Autowired
	private RabbitMQSenderService rabbitMQService;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private IliBusiness iliBusiness;

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
		for (MicroserviceSupplyRequestedDto supplyRequested : requestDto.getSuppliesRequested()) {

			if (supplyRequested.getTypeSupply().getId() == typeSupplyId) {

				// verify if the user's profile matches the input profile
				MicroserviceProviderProfileDto profileSupply = supplyRequested.getTypeSupply().getProviderProfile();
				MicroserviceProviderProfileDto profileUser = userProviderFound.getProfiles().stream()
						.filter(profile -> profileSupply.getId() == profile.getId()).findAny().orElse(null);
				if (profileUser == null) {
					throw new BusinessException(
							"El usuario no tiene asignado el perfil necesario para cargar el tipo de insumo.");
				}

				// verify if the supply is assigned to a task
				List<Long> taskStates = new ArrayList<>(Arrays.asList(TaskBusiness.TASK_STATE_STARTED));
				List<Long> taskCategories = new ArrayList<>(
						Arrays.asList(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION));
				List<MicroserviceTaskDto> tasksDto = taskClient.findByStateAndCategory(taskStates, taskCategories);
				for (MicroserviceTaskDto taskDto : tasksDto) {
					MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
							.filter(meta -> meta.getKey().equalsIgnoreCase("request")).findAny().orElse(null);
					if (metadataRequest instanceof MicroserviceTaskMetadataDto) {

						MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties().stream()
								.filter(p -> p.getKey().equalsIgnoreCase("requestId")).findAny().orElse(null);

						MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties()
								.stream().filter(p -> p.getKey().equalsIgnoreCase("typeSupplyId")).findAny()
								.orElse(null);

						if (propertyRequest != null && propertyTypeSupply != null) {

							if (Long.parseLong(propertyRequest.getValue()) == requestId
									&& Long.parseLong(propertyTypeSupply.getValue()) == typeSupplyId) {

								MicroserviceTaskMemberDto memberDto = taskDto.getMembers().stream()
										.filter(m -> m.getMemberCode() == userCode).findAny().orElse(null);
								if (!(memberDto instanceof MicroserviceTaskMemberDto)) {
									throw new BusinessException(
											"No es posible cargar el insumo, la tarea está asignada a otro usuario.");
								}
							}
						}
					}
				}

				if (supplyRequested.getState().getId() == ProviderBusiness.SUPPLY_REQUESTED_STATE_VALIDATING) {
					throw new BusinessException("Ya se ha cargado un insumo que esta en validación.");
				}

				Long supplyRequestedStateId = null;

				if (delivered == true) {

					// send supply to microservice supplies
					try {
						List<String> urls = new ArrayList<String>();
						if (files.length > 0) {
							for (MultipartFile file : files) {

								String extension = FilenameUtils.getExtension(file.getOriginalFilename());

								MicroserviceExtensionDto extensionDto = supplyRequested.getTypeSupply().getExtensions()
										.stream().filter(ext -> ext.getName().equalsIgnoreCase(extension)).findAny()
										.orElse(null);

								if (!(extensionDto instanceof MicroserviceExtensionDto)) {
									throw new BusinessException("El insumo no cumple los tipos de archivo permitidos.");
								}

								String tmpFile = this.stTemporalDirectory + File.separatorChar
										+ StringUtils.cleanPath(file.getOriginalFilename());
								FileUtils.writeByteArrayToFile(new File(tmpFile), file.getBytes());

								if (extensionDto.getName().equalsIgnoreCase("xtf")) {
									supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_VALIDATING;

									// validate xtf with ilivalidator
									iliBusiness.startValidation(requestId, observations, tmpFile,
											file.getOriginalFilename(), supplyRequested.getId(), userCode,
											supplyRequested.getModelVersion());

								} else {
									supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED;

									// save file with microservice file manager
									String urlBase = "/" + requestDto.getMunicipalityCode().replace(" ", "_")
											+ "/insumos/proveedores/" + providerDto.getName().replace(" ", "_") + "/"
											+ supplyRequested.getTypeSupply().getName().replace(" ", "_");

									String urlDocumentaryRepository = rabbitMQService
											.sendFile(StringUtils.cleanPath(file.getOriginalFilename()), urlBase);

									if (urlDocumentaryRepository == null) {
										throw new BusinessException(
												"No se ha podido guardar el archivo en el repositorio documental.");
									}
									urls.add(urlDocumentaryRepository);

									supplyBusiness.createSupply(requestDto.getMunicipalityCode(), observations,
											typeSupplyId, urls, url, requestId, userCode, providerDto.getId(), null,
											null);

								}
							}
						} else {
							supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED;
						}

					} catch (Exception e) {
						throw new BusinessException(e.getMessage());
					}
				} else {
					supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_UNDELIVERED;
				}

				// Update request
				try {
					MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();
					updateSupply.setDelivered(delivered);
					updateSupply.setSupplyRequestedStateId(supplyRequestedStateId);
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

		for (MicroserviceSupplyRequestedDto supplyRequested : requestDto.getSuppliesRequested()) {
			if (supplyRequested.getState().getId() != ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED
					&& supplyRequested.getState().getId() != ProviderBusiness.SUPPLY_REQUESTED_STATE_UNDELIVERED) {
				throw new BusinessException(
						"No se puede cerrar la solicitud porque no se han cargado todos los insumos.");
			}
		}

		try {

			for (MicroserviceSupplyRequestedDto supplyRequested : requestDto.getSuppliesRequested()) {

				// verify if the supply is assigned to a task
				List<Long> taskStates = new ArrayList<>(Arrays.asList(TaskBusiness.TASK_STATE_STARTED));
				List<Long> taskCategories = new ArrayList<>(
						Arrays.asList(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION));

				List<MicroserviceTaskDto> tasksDto = taskClient.findByStateAndCategory(taskStates, taskCategories);

				for (MicroserviceTaskDto taskDto : tasksDto) {
					MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
							.filter(meta -> meta.getKey().equalsIgnoreCase("request")).findAny().orElse(null);
					if (metadataRequest instanceof MicroserviceTaskMetadataDto) {

						MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties().stream()
								.filter(p -> p.getKey().equalsIgnoreCase("requestId")).findAny().orElse(null);

						MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties()
								.stream().filter(p -> p.getKey().equalsIgnoreCase("typeSupplyId")).findAny()
								.orElse(null);

						if (propertyRequest != null && propertyTypeSupply != null) {

							Long taskRequestId = Long.parseLong(propertyRequest.getValue());
							Long taskTypeSupplyId = Long.parseLong(propertyTypeSupply.getValue());

							if (taskRequestId == requestId
									&& taskTypeSupplyId == supplyRequested.getTypeSupply().getId()) {

								Long supplyRequestedState = supplyRequested.getState().getId();

								if (supplyRequestedState == ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED) {
									taskClient.closeTask(taskDto.getId());
								} else if (supplyRequestedState == ProviderBusiness.SUPPLY_REQUESTED_STATE_UNDELIVERED) {
									MicroserviceCancelTaskDto cancelTaskDto = new MicroserviceCancelTaskDto();
									cancelTaskDto.setReason("Cancelada por el sistema.");
									taskClient.cancelTask(taskDto.getId(), cancelTaskDto);
								}

							}

						}
					}
				}

			}

		} catch (Exception e) {
			log.error("Ha ocurrido un error intentando cerrar las tareas asociadas a la solicitud");
		}

		MicroserviceRequestDto requestUpdatedDto = null;

		try {
			requestUpdatedDto = providerClient.closeRequest(requestId);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido actualizar la información de la solicitud.");
		}

		return requestUpdatedDto;
	}

	public List<MicroserviceProviderUserDto> getUsersByProvider(Long providerId, List<Long> profiles)
			throws BusinessException {

		List<MicroserviceProviderUserDto> usersDto = new ArrayList<>();

		try {

			if (profiles != null) {
				usersDto = providerClient.findUsersByProviderIdAndProfiles(providerId, profiles);
			} else {
				usersDto = providerClient.findUsersByProviderId(providerId);
			}

		} catch (BusinessException e) {
			String message = "No se han podido obtener los usuarios del proveedor.";
			this.log.error(message + ": " + e.getMessage());
			throw new BusinessException(message);
		}

		return usersDto;
	}

}
