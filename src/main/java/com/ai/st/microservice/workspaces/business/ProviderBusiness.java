package com.ai.st.microservice.workspaces.business;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.TaskFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceQueryResultRegistralRevisionDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceAttachmentDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateAttachmentDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateSupplyRevisionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceEmitterDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceExtensionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestPackageDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestPaginatedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRevisionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRevisionDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCancelTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMemberDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataPropertyDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.utils.FileTool;
import com.ai.st.microservice.workspaces.utils.ZipUtil;

@Component
public class ProviderBusiness {

	@Value("${integrations.database.hostname}")
	private String databaseIntegrationHost;

	@Value("${integrations.database.port}")
	private String databaseIntegrationPort;

	@Value("${integrations.database.schema}")
	private String databaseIntegrationSchema;

	@Value("${integrations.database.username}")
	private String databaseIntegrationUsername;

	@Value("${integrations.database.password}")
	private String databaseIntegrationPassword;

	@Value("${st.temporalDirectory}")
	private String stTemporalDirectory;

	@Value("${st.filesDirectory}")
	private String stFilesDirectory;

	// Supplies requested states
	public static final Long SUPPLY_REQUESTED_STATE_ACCEPTED = (long) 1;
	public static final Long SUPPLY_REQUESTED_STATE_REJECTED = (long) 2;
	public static final Long SUPPLY_REQUESTED_STATE_VALIDATING = (long) 3;
	public static final Long SUPPLY_REQUESTED_STATE_PENDING = (long) 4;
	public static final Long SUPPLY_REQUESTED_STATE_UNDELIVERED = (long) 5;
	public static final Long SUPPLY_REQUESTED_STATE_PENDING_REVIEW = (long) 6;
	public static final Long SUPPLY_REQUESTED_STATE_SETTING_REVIEW = (long) 7;
	public static final Long SUPPLY_REQUESTED_STATE_IN_REVIEW = (long) 8;
	public static final Long SUPPLY_REQUESTED_STATE_CLOSING_REVIEW = (long) 9;

	// Requests States
	public static final Long REQUEST_STATE_REQUESTED = (long) 1;
	public static final Long REQUEST_STATE_DELIVERED = (long) 2;
	public static final Long REQUEST_STATE_CANCELLED = (long) 3;

	// Providers
	public static final Long PROVIDER_IGAC_ID = (long) 1;
	public static final Long PROVIDER_SNR_ID = (long) 8;

	// Profiles
	public static final Long PROVIDER_PROFILE_CADASTRAL = (long) 1;

	// Types supplies
	public static final Long PROVIDER_SUPPLY_CADASTRAL = (long) 2;
	public static final Long PROVIDER_SNR_SUPPLY_REGISTRAL = (long) 12;

	private final Logger log = LoggerFactory.getLogger(ProviderBusiness.class);

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private TaskFeignClient taskClient;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private IliBusiness iliBusiness;

	@Autowired
	private FileBusiness fileBusiness;

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private ManagerBusiness managerBusiness;

	@Autowired
	private UserBusiness userBusiness;

	@Autowired
	private IMunicipalityService municipalityService;

	@Autowired
	private DatabaseIntegrationBusiness databaseIntegrationBusiness;

	@Autowired
	private CrytpoBusiness cryptoBusiness;

	@Autowired
	private FTPBusiness ftpBusiness;

	public MicroserviceRequestDto answerRequest(Long requestId, Long typeSupplyId, String justification,
			MultipartFile[] files, String url, MicroserviceProviderDto providerDto, Long userCode, String observations)
			throws BusinessException {

		MicroserviceRequestDto requestUpdatedDto = null;

		if (files.length == 0 && (url == null || url.isEmpty()) && (justification == null || justification.isEmpty())) {
			throw new BusinessException("Se debe justificar porque no se cargará el insumo.");
		}

		MicroserviceRequestDto requestDto = providerClient.findRequestById(requestId);

		if (!providerDto.getId().equals(requestDto.getProvider().getId())) {
			throw new BusinessException("No tiene acceso a la solicitud.");
		}

		if (!requestDto.getRequestState().getId().equals(ProviderBusiness.REQUEST_STATE_REQUESTED)) {
			throw new BusinessException("La solicitud esta cerrada, no se puede modificar.");
		}

		Boolean delivered = (files.length > 0 || (url != null && !url.isEmpty())) ? true : false;
		if (delivered && (observations == null || observations.isEmpty())) {
			throw new BusinessException("Las observaciones son requeridas.");
		}

		List<MicroserviceProviderUserDto> usersByProvider = providerClient.findUsersByProviderId(providerDto.getId());
		MicroserviceProviderUserDto userProviderFound = usersByProvider.stream()
				.filter(user -> userCode.equals(user.getUserCode())).findAny().orElse(null);
		if (userProviderFound == null) {
			throw new BusinessException("El usuario no esta registrado como usuario para el proveedor de insumo.");
		}

		MicroserviceSupplyRequestedDto supplyRequested = requestDto.getSuppliesRequested().stream()
				.filter(sR -> sR.getTypeSupply().getId().equals(typeSupplyId)).findAny().orElse(null);

		if (supplyRequested == null) {
			throw new BusinessException("El tipo de insumo no pertenece a la solicitud.");
		}

		// verify if the user's profile matches the input profile
		MicroserviceProviderProfileDto profileSupply = supplyRequested.getTypeSupply().getProviderProfile();
		MicroserviceProviderProfileDto profileUser = userProviderFound.getProfiles().stream()
				.filter(profile -> profileSupply.getId().equals(profile.getId())).findAny().orElse(null);
		if (profileUser == null) {
			throw new BusinessException(
					"El usuario no tiene asignado el perfil necesario para cargar el tipo de insumo.");
		}

		try {

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

					MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties().stream()
							.filter(p -> p.getKey().equalsIgnoreCase("typeSupplyId")).findAny().orElse(null);

					if (propertyRequest != null && propertyTypeSupply != null) {

						Long metaRequestId = Long.parseLong(propertyRequest.getValue());
						Long metaTypeSupplyId = Long.parseLong(propertyTypeSupply.getValue());

						if (metaRequestId.equals(requestId) && metaTypeSupplyId.equals(typeSupplyId)) {

							MicroserviceTaskMemberDto memberDto = taskDto.getMembers().stream()
									.filter(m -> m.getMemberCode().equals(userCode)).findAny().orElse(null);
							if (!(memberDto instanceof MicroserviceTaskMemberDto)) {
								throw new BusinessException(
										"No es posible cargar el insumo, la tarea está asignada a otro usuario.");
							}
						}
					}
				}
			}

		} catch (Exception e) {
			log.error("No se ha podido consultar si la tarea esta asociada al cargue de insumo: " + e.getMessage());
		}

		if (supplyRequested.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_VALIDATING)) {
			throw new BusinessException("Ya se ha cargado un insumo que esta en validación.");
		}
		if (supplyRequested.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_PENDING_REVIEW)
				|| supplyRequested.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_SETTING_REVIEW)
				|| supplyRequested.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_IN_REVIEW)
				|| supplyRequested.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_CLOSING_REVIEW)) {
			throw new BusinessException("No se puede cargar el insumo porque el insumo esta en revisión.");
		}

		Long supplyRequestedStateId = null;

		MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();

		if (delivered == true) {

			// send supply to microservice supplies

			List<String> urls = new ArrayList<String>();
			if (files.length > 0) {
				for (MultipartFile fileUploaded : files) {

					String loadedFileName = fileUploaded.getOriginalFilename();
					String loadedFileExtension = FilenameUtils.getExtension(loadedFileName);

					String fileNameRandom = RandomStringUtils.random(14, true, false) + "." + loadedFileExtension;
					String filePathTemporal = fileBusiness.loadFileToSystem(fileUploaded, fileNameRandom);

					Boolean zipFile = false;

					List<MicroserviceExtensionDto> extensionAllowed = supplyRequested.getTypeSupply().getExtensions();

					Boolean fileAllowed = false;
					Boolean isLoadShp = false;
					List<String> loadedFileExtensions = new ArrayList<>();

					// verify if the supply is a shp file
					MicroserviceExtensionDto extensionShpDto = extensionAllowed.stream()
							.filter(ext -> ext.getName().equalsIgnoreCase("shp")).findAny().orElse(null);
					if (extensionShpDto != null) {

						List<String> extensionsShp = new ArrayList<String>(Arrays.asList("shp", "dbf", "shx", "prj"));

						if (loadedFileExtension.equalsIgnoreCase("zip")) {

							isLoadShp = ZipUtil.zipContainsFile(filePathTemporal, extensionsShp);
							if (isLoadShp) {
								fileAllowed = ZipUtil.zipMustContains(filePathTemporal, extensionsShp);
								loadedFileExtensions = extensionsShp;
								zipFile = false;
							}

						}

					}

					if (loadedFileExtension.equalsIgnoreCase("zip")) {

						if (!isLoadShp) {
							List<String> extensionsAllowed = extensionAllowed.stream().map(ext -> {
								return ext.getName();
							}).collect(Collectors.toList());

							fileAllowed = ZipUtil.zipContainsFile(filePathTemporal, extensionsAllowed);
							loadedFileExtensions = ZipUtil.getExtensionsFromZip(filePathTemporal);
							zipFile = false;
						}

					} else {

						MicroserviceExtensionDto extensionDto = extensionAllowed.stream()
								.filter(ext -> ext.getName().equalsIgnoreCase(loadedFileExtension)).findAny()
								.orElse(null);
						fileAllowed = extensionDto instanceof MicroserviceExtensionDto;
						loadedFileExtensions.add(loadedFileExtension);
						zipFile = true;
					}

					if (!fileAllowed) {
						throw new BusinessException("El insumo no cumple los tipos de archivo permitidos.");
					}

					String supplyExtension = loadedFileExtensions.stream().filter(ext -> ext.equalsIgnoreCase("xtf"))
							.findAny().orElse("");

					try {
						FileUtils.deleteQuietly(new File(filePathTemporal));
					} catch (Exception e) {
						log.error("No se ha podido eliminar el archivo temporal: " + e.getMessage());
					}

					// save file
					String urlBase = "/" + requestDto.getMunicipalityCode().replace(" ", "_") + "/insumos/proveedores/"
							+ providerDto.getName().replace(" ", "_") + "/"
							+ supplyRequested.getTypeSupply().getName().replace(" ", "_");

					urlBase = FileTool.removeAccents(urlBase);

					String urlDocumentaryRepository = fileBusiness.saveFileToSystem(fileUploaded, urlBase, zipFile);

					if (!supplyExtension.isEmpty()) {
						supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_VALIDATING;

						// validate xtf with ilivalidator
						iliBusiness.startValidation(requestId, observations, urlDocumentaryRepository,
								urlDocumentaryRepository, supplyRequested.getId(), userCode,
								supplyRequested.getModelVersion());

						updateSupply.setUrl(null);
						updateSupply.setFtp(null);

					} else {

						supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED;

						urls.add(urlDocumentaryRepository);

						updateSupply.setUrl(urls.get(0));
						updateSupply.setObservations(observations);

					}
				}
			} else {
				updateSupply.setFtp(url);
				updateSupply.setObservations(observations);
				supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED;
			}

		} else {
			supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_UNDELIVERED;
		}

		log.info("Update request # " + requestId + " - " + supplyRequested.getId());

		// Update request
		try {

			updateSupply.setDelivered(delivered);
			updateSupply.setDeliveryBy(userCode);
			updateSupply.setSupplyRequestedStateId(supplyRequestedStateId);
			updateSupply.setJustification(justification);
			requestUpdatedDto = providerClient.updateSupplyRequested(requestId, supplyRequested.getId(), updateSupply);

			for (MicroserviceSupplyRequestedDto supply : requestUpdatedDto.getSuppliesRequested()) {
				if (supply.getDeliveredBy() != null) {
					try {
						MicroserviceUserDto userDto = userClient.findById(supply.getDeliveredBy());
						supply.setUserDeliveryBy(userDto);
					} catch (Exception e) {
						supply.setUserDeliveryBy(null);
					}
				}
			}

		} catch (Exception e) {
			throw new BusinessException("No se ha podido actualizar la información de la solicitud.");
		}

		return requestUpdatedDto;
	}

	public MicroserviceRequestDto closeRequest(Long requestId, MicroserviceProviderDto providerDto, Long userCode)
			throws BusinessException {

		MicroserviceRequestDto requestDto = providerClient.findRequestById(requestId);

		if (!providerDto.getId().equals(requestDto.getProvider().getId())) {
			throw new BusinessException("No tiene acceso a la solicitud.");
		}

		if (!requestDto.getRequestState().getId().equals(ProviderBusiness.REQUEST_STATE_REQUESTED)) {
			throw new BusinessException("La solicitud esta cerrada, no se puede cerrar.");
		}

		List<MicroserviceProviderUserDto> usersByProvider = providerClient.findUsersByProviderId(providerDto.getId());
		MicroserviceProviderUserDto userProviderFound = usersByProvider.stream()
				.filter(user -> userCode.equals(user.getUserCode())).findAny().orElse(null);
		if (userProviderFound == null) {
			throw new BusinessException("El usuario no esta registrado como usuario para el proveedor de insumo.");
		}

		Boolean canClose = false;
		for (MicroserviceSupplyRequestedDto supplyRequested : requestDto.getSuppliesRequested()) {
			if (!supplyRequested.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED)
					&& !supplyRequested.getState().getId()
							.equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_UNDELIVERED)) {
				throw new BusinessException(
						"No se puede cerrar la solicitud porque no se han cargado todos los insumos.");
			}

			if (supplyRequested.getDeliveredBy().equals(userCode)) {
				canClose = true;
			}
		}

		if (!canClose) {
			throw new BusinessException(
					"No se puede cerrar la solicitud porque el usuario no es la persona que ha cargado los insumos.");
		}

		Boolean sendToReview = false;
		MicroserviceSupplyRequestedDto supplyRegistral = null;
		if (requestDto.getProvider().getId().equals(ProviderBusiness.PROVIDER_SNR_ID)) {

			supplyRegistral = requestDto.getSuppliesRequested().stream()
					.filter(sR -> sR.getTypeSupply().getId().equals(ProviderBusiness.PROVIDER_SNR_SUPPLY_REGISTRAL))
					.findAny().orElse(null);
			sendToReview = (supplyRegistral != null);
		}

		MicroserviceRequestDto requestUpdatedDto = null;

		if (sendToReview) {

			// Update supply requested
			try {

				MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();
				updateSupply.setDelivered(null);
				updateSupply.setDeliveryBy(null);
				updateSupply.setSupplyRequestedStateId(ProviderBusiness.SUPPLY_REQUESTED_STATE_PENDING_REVIEW);
				updateSupply.setJustification(null);
				requestUpdatedDto = providerClient.updateSupplyRequested(requestId, supplyRegistral.getId(),
						updateSupply);

				for (MicroserviceSupplyRequestedDto supply : requestUpdatedDto.getSuppliesRequested()) {
					if (supply.getDeliveredBy() != null) {
						try {
							MicroserviceUserDto userDto = userClient.findById(supply.getDeliveredBy());
							supply.setUserDeliveryBy(userDto);
						} catch (Exception e) {
							supply.setUserDeliveryBy(null);
						}
					}
				}

			} catch (Exception e) {
				throw new BusinessException("No se ha podido actualizar la información de la solicitud.");
			}

		} else {

			try {

				for (MicroserviceSupplyRequestedDto supplyRequested : requestDto.getSuppliesRequested()) {

					// verify if the supply is assigned to a task
					List<Long> taskStates = new ArrayList<>(
							Arrays.asList(TaskBusiness.TASK_STATE_STARTED, TaskBusiness.TASK_STATE_ASSIGNED));
					List<Long> taskCategories = new ArrayList<>(
							Arrays.asList(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION));

					List<MicroserviceTaskDto> tasksDto = taskClient.findByStateAndCategory(taskStates, taskCategories);

					for (MicroserviceTaskDto taskDto : tasksDto) {
						MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
								.filter(meta -> meta.getKey().equalsIgnoreCase("request")).findAny().orElse(null);
						if (metadataRequest instanceof MicroserviceTaskMetadataDto) {

							MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties()
									.stream().filter(p -> p.getKey().equalsIgnoreCase("requestId")).findAny()
									.orElse(null);

							MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties()
									.stream().filter(p -> p.getKey().equalsIgnoreCase("typeSupplyId")).findAny()
									.orElse(null);

							if (propertyRequest != null && propertyTypeSupply != null) {

								Long taskRequestId = Long.parseLong(propertyRequest.getValue());
								Long taskTypeSupplyId = Long.parseLong(propertyTypeSupply.getValue());

								if (taskRequestId.equals(requestId)
										&& taskTypeSupplyId.equals(supplyRequested.getTypeSupply().getId())) {

									Long supplyRequestedState = supplyRequested.getState().getId();

									if (supplyRequestedState.equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED)) {
										taskClient.closeTask(taskDto.getId());
									} else if (supplyRequestedState
											.equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_UNDELIVERED)) {
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

			try {

				for (MicroserviceSupplyRequestedDto supplyRequested : requestDto.getSuppliesRequested()) {

					if (supplyRequested.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED)) {

						List<String> urls = new ArrayList<>();
						if (supplyRequested.getUrl() != null) {
							urls.add(supplyRequested.getUrl());
						}

						supplyBusiness.createSupply(requestDto.getMunicipalityCode(), supplyRequested.getObservations(),
								supplyRequested.getTypeSupply().getId(), urls, supplyRequested.getFtp(), requestId,
								userCode, providerDto.getId(), null, supplyRequested.getModelVersion());
					}

				}

			} catch (Exception e) {
				log.error("No se ha podido crear los insumos: " + e.getMessage());
				throw new BusinessException("No se ha podido disponer los insumos al municipio.");
			}

			try {
				requestUpdatedDto = providerClient.closeRequest(requestId, userCode);
			} catch (Exception e) {
				throw new BusinessException("No se ha podido actualizar la información de la solicitud.");
			}

		}

		return requestUpdatedDto;
	}

	public MicroserviceRequestDto closeRequest(Long requestId, Long userCode) {

		MicroserviceRequestDto requestDto = null;

		try {
			requestDto = providerClient.closeRequest(requestId, userCode);
		} catch (Exception e) {
			log.error("Error cerrando solicitud: " + e.getMessage());
		}

		return requestDto;
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

	public List<MicroserviceRequestDto> getRequestsByEmmitersManager(Long managerCode) throws BusinessException {

		List<MicroserviceRequestDto> listRequestsDto = new ArrayList<>();

		try {

			listRequestsDto = providerClient.findRequestsByEmmiters(managerCode, "ENTITY");

		} catch (Exception e) {
			log.error("Error consultando solicitudes: " + e.getMessage());
		}

		return listRequestsDto;
	}

	public MicroserviceRequestPaginatedDto getRequestsByManagerAndMunicipality(int page, Long managerCode,
			String municipalityCode) throws BusinessException {

		MicroserviceRequestPaginatedDto data = null;

		try {

			data = providerClient.getRequestsByManagerAndMunicipality(managerCode, municipalityCode, page);

			List<MicroserviceRequestDto> requests = data.getItems();
			for (MicroserviceRequestDto requestDto : requests) {
				requestDto = this.completeInformationRequest(requestDto);
			}

		} catch (BusinessException e) {
			log.error("Error consultando solicitudes por gestor y municipio: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error consultando solicitudes por gestor y municipio: " + e.getMessage());
			throw new BusinessException("No se ha podido consultar las solicitudes que el gestor ha realizado.");
		}

		return data;
	}

	public MicroserviceRequestPaginatedDto getRequestsByManagerAndProvider(int page, Long managerCode, Long providerId)
			throws BusinessException {

		MicroserviceRequestPaginatedDto data = null;

		try {

			data = providerClient.getRequestsByManagerAndProvider(managerCode, providerId, page);

			List<MicroserviceRequestDto> requests = data.getItems();
			for (MicroserviceRequestDto requestDto : requests) {
				requestDto = this.completeInformationRequest(requestDto);
			}

		} catch (BusinessException e) {
			log.error("Error consultando solicitudes por gestor y proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error consultando solicitudes por gestor y proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido consultar las solicitudes que el gestor ha realizado.");
		}

		return data;
	}

	public List<MicroserviceRequestPackageDto> getRequestsByManagerAndPackage(Long managerCode, String packageLabel)
			throws BusinessException {

		List<MicroserviceRequestPackageDto> packages = new ArrayList<>();
		List<MicroserviceRequestDto> requests = new ArrayList<>();

		List<String> labels = new ArrayList<String>();

		try {

			if (packageLabel != null) {
				requests = providerClient.getRequestsByManagerAndPackage(managerCode, packageLabel);
			} else {
				requests = providerClient.findRequestsByEmmiters(managerCode, "ENTITY");
			}

			for (MicroserviceRequestDto requestDto : requests) {

				requestDto = this.completeInformationRequest(requestDto);

				String packageRequest = requestDto.getPackageLabel();

				if (!labels.contains(packageRequest)) {
					MicroserviceRequestPackageDto data = new MicroserviceRequestPackageDto();
					data.setPackageLabel(packageRequest);
					data.getRequests().add(requestDto);
					packages.add(data);
				} else {

					MicroserviceRequestPackageDto packageFound = packages.stream()
							.filter(p -> p.getPackageLabel().equals(packageRequest)).findAny().orElse(null);
					if (packageFound instanceof MicroserviceRequestPackageDto) {
						packageFound.getRequests().add(requestDto);
					}

				}

			}

		} catch (BusinessException e) {
			log.error("Error consultando solicitudes por gestor y proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error consultando solicitudes por gestor y proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido consultar las solicitudes que el gestor ha realizado.");
		}

		return packages;
	}

	public MicroserviceRequestDto completeInformationRequest(MicroserviceRequestDto requestDto) {

		List<MicroserviceEmitterDto> emittersDto = new ArrayList<MicroserviceEmitterDto>();
		for (MicroserviceEmitterDto emitterDto : requestDto.getEmitters()) {
			if (emitterDto.getEmitterType().equals("ENTITY")) {
				try {
					MicroserviceManagerDto managerDto = managerBusiness.getManagerById(emitterDto.getEmitterCode());
					emitterDto.setUser(managerDto);
				} catch (Exception e) {
					emitterDto.setUser(null);
				}
			} else {
				try {
					MicroserviceUserDto userDto = userBusiness.getUserById(emitterDto.getEmitterCode());
					emitterDto.setUser(userDto);
				} catch (Exception e) {
					emitterDto.setUser(null);
				}
			}
			emittersDto.add(emitterDto);
		}

		MunicipalityEntity municipalityEntity = municipalityService
				.getMunicipalityByCode(requestDto.getMunicipalityCode());

		if (municipalityEntity instanceof MunicipalityEntity) {
			DepartmentEntity departmentEntity = municipalityEntity.getDepartment();

			MunicipalityDto municipalityDto = new MunicipalityDto();
			municipalityDto.setCode(municipalityEntity.getCode());
			municipalityDto.setId(municipalityEntity.getId());
			municipalityDto.setName(municipalityEntity.getName());
			municipalityDto.setDepartment(new DepartmentDto(departmentEntity.getId(), departmentEntity.getName(),
					departmentEntity.getCode()));

			requestDto.setEmitters(emittersDto);
			requestDto.setMunicipality(municipalityDto);
		}

		for (MicroserviceSupplyRequestedDto supply : requestDto.getSuppliesRequested()) {

			if (supply.getDeliveredBy() != null) {

				try {

					MicroserviceUserDto userDto = userBusiness.getUserById(supply.getDeliveredBy());
					supply.setUserDeliveryBy(userDto);
				} catch (Exception e) {
					supply.setUserDeliveryBy(null);
				}

			}

		}

		return requestDto;
	}

	public MicroserviceProviderDto getProviderById(Long providerId) {

		MicroserviceProviderDto providerDto = null;

		try {
			providerDto = providerClient.findById(providerId);
		} catch (Exception e) {
			log.error("No se podido consultar el proveedor: " + e.getMessage());
		}

		return providerDto;
	}

	public MicroserviceProviderProfileDto createProfile(Long providerId, String name, String description)
			throws BusinessException {

		MicroserviceProviderProfileDto profileDto = null;

		try {

			MicroserviceCreateProviderProfileDto createProviderProfileDto = new MicroserviceCreateProviderProfileDto();
			createProviderProfileDto.setName(name);
			createProviderProfileDto.setDescription(description);

			profileDto = providerClient.createProfile(providerId, createProviderProfileDto);

		} catch (BusinessException e) {
			log.error("Error creando perfil del proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error creando perfil del proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido crear el perfil del proveedor");
		}

		return profileDto;
	}

	public List<MicroserviceProviderProfileDto> getProfilesByProvider(Long providerId) throws BusinessException {

		List<MicroserviceProviderProfileDto> profilesDto = new ArrayList<>();

		try {

			profilesDto = providerClient.getProfilesByProvider(providerId);

		} catch (BusinessException e) {
			log.error("Error consultando perfiles del proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error consultando perfiles del proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido consultar los perfiles del proveedor");
		}

		return profilesDto;
	}

	public MicroserviceProviderProfileDto updateProfile(Long providerId, Long profileId, String name,
			String description) throws BusinessException {

		MicroserviceProviderProfileDto profileDto = null;

		try {

			MicroserviceCreateProviderProfileDto createProviderProfileDto = new MicroserviceCreateProviderProfileDto();
			createProviderProfileDto.setName(name);
			createProviderProfileDto.setDescription(description);

			profileDto = providerClient.updateProfile(providerId, profileId, createProviderProfileDto);

		} catch (BusinessException e) {
			log.error("Error editando perfil del proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error editando perfil del proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido editar el perfil del proveedor");
		}

		return profileDto;
	}

	public void deleteProfile(Long providerId, Long profileId) throws BusinessException {

		try {

			providerClient.deleteProfile(providerId, profileId);

		} catch (BusinessException e) {
			log.error("Error eliminando perfil del proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error eliminando perfil del proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido eliminar el perfil del proveedor");
		}
	}

	public MicroserviceTypeSupplyDto createTypeSupply(Long providerId, String name, String description,
			Boolean metadataRequired, Boolean modelRequired, Long profileId, List<String> extensions)
			throws BusinessException {

		MicroserviceTypeSupplyDto typeSupplyDto = null;

		try {

			MicroserviceCreateTypeSupplyDto create = new MicroserviceCreateTypeSupplyDto();
			create.setName(name);
			create.setDescription(description);
			create.setExtensions(extensions);
			create.setMetadataRequired(metadataRequired);
			create.setModelRequired(modelRequired);
			create.setProviderProfileId(profileId);

			typeSupplyDto = providerClient.createTypeSupplies(providerId, create);

		} catch (BusinessException e) {
			log.error("Error creando tipo de insumo para el proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error creando tipo de insumo para el proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido crear el tipo de insumo para el proveedor");
		}

		return typeSupplyDto;
	}

	public List<MicroserviceTypeSupplyDto> getTypesSuppliesByProvider(Long providerId) throws BusinessException {

		List<MicroserviceTypeSupplyDto> typesSuppliesDto = new ArrayList<>();

		try {

			typesSuppliesDto = providerClient.getTypesSuppliesByProvider(providerId);

		} catch (BusinessException e) {
			log.error("Error consultando tipos de insumo del proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error consultando tipos de insumo del proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido consultar los tipos de insumo del proveedor");
		}

		return typesSuppliesDto;
	}

	public MicroserviceTypeSupplyDto updateTypeSupply(Long providerId, Long typeSupplyId, String name,
			String description, Boolean metadataRequired, Boolean modelRequired, Long profileId,
			List<String> extensions) throws BusinessException {

		MicroserviceTypeSupplyDto typeSupplyDto = null;

		try {

			MicroserviceCreateTypeSupplyDto data = new MicroserviceCreateTypeSupplyDto();
			data.setName(name);
			data.setDescription(description);
			data.setExtensions(extensions);
			data.setMetadataRequired(metadataRequired);
			data.setModelRequired(modelRequired);
			data.setProviderProfileId(profileId);

			typeSupplyDto = providerClient.updateTypeSupplies(providerId, typeSupplyId, data);

		} catch (BusinessException e) {
			log.error("Error editando tipo de insumo para el proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error editando tipo de insumo para el proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido editar el tipo de insumo para el proveedor");
		}

		return typeSupplyDto;
	}

	public void deleteTypeSupply(Long providerId, Long typeSupplyId) throws BusinessException {

		try {

			providerClient.deleteTypeSupply(providerId, typeSupplyId);

		} catch (BusinessException e) {
			log.error("Error eliminando tipo de insumo del proveedor: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error eliminando tipo de insumo del proveedor: " + e.getMessage());
			throw new BusinessException("No se ha podido eliminar el tipo de insumo del proveedor");
		}
	}

	public MicroserviceProviderDto addProvider(MicroserviceCreateProviderDto createProviderDto) {
		MicroserviceProviderDto providerDto = null;
		try {
			providerDto = providerClient.addProvider(createProviderDto);
		} catch (Exception e) {
			log.error("No se ha podido agregar el gestor: " + e.getMessage());
		}
		return providerDto;
	}

	public MicroserviceProviderDto updateProvider(MicroserviceUpdateProviderDto updateProviderDto) {
		MicroserviceProviderDto providerDto = null;
		try {
			providerDto = providerClient.updateProvider(updateProviderDto);
		} catch (Exception e) {
			log.error("No se ha podido agregar el gestor: " + e.getMessage());
		}
		return providerDto;
	}

	public MicroserviceProviderDto getProviderByUserAdministrator(Long userCode) {

		MicroserviceProviderDto providerDto = null;

		try {
			providerDto = providerClient.findProviderByAdministrator(userCode);
		} catch (Exception e) {
			log.error("No se ha podido consultar el proveedor: " + e.getMessage());
		}

		return providerDto;
	}

	public MicroserviceProviderDto getProviderByUserTechnical(Long userCode) {

		MicroserviceProviderDto providerDto = null;

		try {
			providerDto = providerClient.findByUserCode(userCode);
		} catch (Exception e) {
			log.error("No se ha podido consultar el proveedor: " + e.getMessage());
		}

		return providerDto;
	}

	public MicroserviceProviderDto getProviderByUserTechnicalOrAdministrator(Long userCode) {

		MicroserviceProviderDto providerDtoByAdministrator = getProviderByUserAdministrator(userCode);
		MicroserviceProviderDto providerDtoByTechnical = getProviderByUserTechnical(userCode);

		if (providerDtoByAdministrator != null) {
			return providerDtoByAdministrator;
		}

		if (providerDtoByTechnical != null) {
			return providerDtoByTechnical;
		}

		return null;
	}

	public boolean userProviderIsDirector(Long userCode) {

		Boolean isDirector = false;

		try {

			List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
					.findRolesByUser(userCode);

			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = providerRoles.stream()
					.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
					.orElse(null);

			if (roleDirector != null) {
				isDirector = true;
			}

		} catch (Exception e) {
			log.error("No se ha podido verificar si el usuario es un director(proveedor): " + e.getMessage());
		}

		return isDirector;
	}

	public boolean userProviderIsDelegate(Long userCode) {

		Boolean isDelegate = false;

		try {

			List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> providerRoles = providerClient
					.findRolesByUser(userCode);

			com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = providerRoles.stream()
					.filter(roleDto -> roleDto.getId().equals(RoleBusiness.SUB_ROLE_DELEGATE_PROVIDER)).findAny()
					.orElse(null);

			if (roleDirector != null) {
				isDelegate = true;
			}

		} catch (Exception e) {
			log.error("No se ha podido verificar si el usuario es un delegado(proveedor): " + e.getMessage());
		}

		return isDelegate;
	}

	public List<MicroserviceSupplyRequestedDto> getSuppliesToReview(Long providerId) {

		List<MicroserviceSupplyRequestedDto> suppliesRequestedDto = new ArrayList<>();

		try {

			List<Long> states = new ArrayList<>(Arrays.asList(ProviderBusiness.SUPPLY_REQUESTED_STATE_PENDING_REVIEW,
					ProviderBusiness.SUPPLY_REQUESTED_STATE_SETTING_REVIEW,
					ProviderBusiness.SUPPLY_REQUESTED_STATE_IN_REVIEW,
					ProviderBusiness.SUPPLY_REQUESTED_STATE_CLOSING_REVIEW));

			suppliesRequestedDto = providerClient.getSuppliesRequestedToReview(providerId, states);

		} catch (Exception e) {
			log.error("No se ha podido consultar los insumos pendiente de revisión: " + e.getMessage());
		}

		return suppliesRequestedDto;
	}

	public MicroserviceSupplyRequestedDto getSupplyRequestedById(Long supplyRequestedId) {

		MicroserviceSupplyRequestedDto supplyRequestedDto = null;

		try {

			supplyRequestedDto = providerClient.getSupplyRequested(supplyRequestedId);

		} catch (Exception e) {
			log.error("No se ha podido consultar el insumo solicitado: " + e.getMessage());
		}

		return supplyRequestedDto;
	}

	public MicroserviceSupplyRevisionDto createSupplyRevision(Long supplyRequestedId, String database, String hostname,
			String username, String password, String port, String schema, Long startBy) {

		MicroserviceSupplyRevisionDto supplyRevisionDto = null;

		try {

			MicroserviceCreateSupplyRevisionDto createRevisionDto = new MicroserviceCreateSupplyRevisionDto();
			createRevisionDto.setDatabase(database);
			createRevisionDto.setHostname(hostname);
			createRevisionDto.setPassword(password);
			createRevisionDto.setPort(port);
			createRevisionDto.setSchema(schema);
			createRevisionDto.setStartBy(startBy);
			createRevisionDto.setUsername(username);

			supplyRevisionDto = providerClient.createSupplyRevision(supplyRequestedId, createRevisionDto);

		} catch (Exception e) {
			log.error("No se ha podido crear la revisión: " + e.getMessage());
		}

		return supplyRevisionDto;
	}

	public MicroserviceRequestDto updateStateToSupplyRequested(Long requestId, Long supplyRequestedId, Long stateId) {

		MicroserviceRequestDto requestDto = null;

		try {

			MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();
			updateSupply.setSupplyRequestedStateId(stateId);

			providerClient.updateSupplyRequested(requestId, supplyRequestedId, updateSupply);
		} catch (Exception e) {
			log.error("No se ha podido actualizar el estado del insumo solicitado: " + e.getMessage());
		}

		return requestDto;
	}

	public MicroserviceRequestDto updateSupplyRequested(Long requestId, Long supplyRequestedId,
			MicroserviceUpdateSupplyRequestedDto updateSupplyData) {

		MicroserviceRequestDto requestDto = null;

		try {

			providerClient.updateSupplyRequested(requestId, supplyRequestedId, updateSupplyData);
		} catch (Exception e) {
			log.error("No se ha podido actualizar el insumo solicitado: " + e.getMessage());
		}

		return requestDto;
	}

	public void startRevision(Long supplyRequestedId, Long userCode, MicroserviceProviderDto prodiverDto)
			throws BusinessException {

		MicroserviceSupplyRequestedDto supplyRequestedDto = this.getSupplyRequestedById(supplyRequestedId);
		if (supplyRequestedDto == null) {
			throw new BusinessException("El insumo solicitado no existe");
		}

		if (!supplyRequestedDto.getTypeSupply().getProvider().getId().equals(prodiverDto.getId())) {
			throw new BusinessException("El insumo solicitado no pertenece al proveedor");
		}

		if (!supplyRequestedDto.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_PENDING_REVIEW)) {
			throw new BusinessException("El estado en el que se encuentra el insumo no permite iniciar una revisión.");
		}

		String randomUsername = RandomStringUtils.random(8, true, false).toLowerCase();
		String randomPassword = RandomStringUtils.random(10, true, true);

		// create FTP credentials
		boolean credentialsCreated = ftpBusiness.createFTPCredentials(randomUsername, randomPassword);
		if (!credentialsCreated) {
			throw new BusinessException("No se ha podido crear el espacio para el almacenamiento de archivos.");
		}

		// create database
		String randomDatabaseName = RandomStringUtils.random(8, true, false).toLowerCase();
		String schema = "import_snr";
		try {
			databaseIntegrationBusiness.createDatabase(randomDatabaseName, randomUsername, randomPassword);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido iniciar la revisión.");
		}

		// create revision
		MicroserviceSupplyRevisionDto supplyRevisionDto = null;
		try {
			supplyRevisionDto = createSupplyRevision(supplyRequestedId, cryptoBusiness.encrypt(randomDatabaseName),
					cryptoBusiness.encrypt(databaseIntegrationHost), cryptoBusiness.encrypt(randomUsername),
					cryptoBusiness.encrypt(randomPassword), cryptoBusiness.encrypt(databaseIntegrationPort),
					cryptoBusiness.encrypt(schema), userCode);
		} catch (Exception e) {
			throw new BusinessException("Ha ocurrido un error intentando crear la revisión.");
		}
		if (supplyRevisionDto == null) {
			throw new BusinessException("Ha ocurrido un error intentando crear la revisión.");
		}

		// start import
		try {

			String reference = "import-" + supplyRequestedDto.getId() + "-" + supplyRequestedDto.getRequest().getId();

			iliBusiness.startImport(supplyRequestedDto.getUrl(), databaseIntegrationHost, randomDatabaseName,
					databaseIntegrationPassword, databaseIntegrationPort, schema, databaseIntegrationUsername,
					reference, supplyRequestedDto.getModelVersion().trim(), IliBusiness.ILI_CONCEPT_INTEGRATION);

		} catch (Exception e) {
			log.error("No se ha podido iniciar la revisión: " + e.getMessage());
			throw new BusinessException("No se ha podido iniciar la revisión.");
		}

		// update state supply requested to Setting Revision
		updateStateToSupplyRequested(supplyRequestedDto.getRequest().getId(), supplyRequestedId,
				ProviderBusiness.SUPPLY_REQUESTED_STATE_SETTING_REVIEW);
	}

	public MicroserviceSupplyRevisionDto getSupplyRevisionFromSupplyRequested(Long supplyRequestedId) {

		MicroserviceSupplyRevisionDto supplyRevisionDto = null;

		try {

			supplyRevisionDto = providerClient.getSupplyRevisionFromSupplyRequested(supplyRequestedId);

		} catch (Exception e) {
			log.error("No se ha podido consultar la revisión del insumo solicitado: " + e.getMessage());
		}

		return supplyRevisionDto;
	}

	public void deleteSupplyRevision(Long supplyRequestedId, Long supplyRevisionId) {

		try {

			providerClient.deleteSupplyRevision(supplyRequestedId, supplyRevisionId);

		} catch (Exception e) {
			log.error("No se ha podido eliminar la revisión del insumo solicitado: " + e.getMessage());
		}
	}

	public MicroserviceQueryResultRegistralRevisionDto getRecordsFromRevision(MicroserviceProviderDto prodiverDto,
			Long supplyRequestedId, int page) throws BusinessException {

		if (page <= 0) {
			page = 1;
		}

		MicroserviceSupplyRequestedDto supplyRequestedDto = this.getSupplyRequestedById(supplyRequestedId);
		if (supplyRequestedDto == null) {
			throw new BusinessException("El insumo solicitado no existe");
		}

		if (!supplyRequestedDto.getTypeSupply().getProvider().getId().equals(prodiverDto.getId())) {
			throw new BusinessException("El insumo solicitado no pertenece al proveedor");
		}

		if (!supplyRequestedDto.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_IN_REVIEW)) {
			throw new BusinessException("El estado en el que se encuentra el insumo no permite realizar la consulta.");
		}

		MicroserviceSupplyRevisionDto supplyRevisionDto = getSupplyRevisionFromSupplyRequested(supplyRequestedId);
		if (supplyRevisionDto == null) {
			throw new BusinessException("No existe una revisión para el insumo.");
		}

		MicroserviceQueryResultRegistralRevisionDto resultDto = null;

		try {

			String host = cryptoBusiness.decrypt(supplyRevisionDto.getHostname());
			String database = cryptoBusiness.decrypt(supplyRevisionDto.getDatabase());
			String password = databaseIntegrationPassword;
			String port = cryptoBusiness.decrypt(supplyRevisionDto.getPort());
			String schema = cryptoBusiness.decrypt(supplyRevisionDto.getSchema());
			String username = databaseIntegrationUsername;

			int limit = 1000;

			resultDto = iliBusiness.getResultQueryRegistralRevision(host, database, password, port, schema, username,
					supplyRequestedDto.getModelVersion(), page, limit);

		} catch (Exception e) {
			log.error("No se ha podido realizar la consulta: " + e.getMessage());
			throw new BusinessException("No se ha podido realizar la consulta.");
		}

		if (resultDto == null) {
			throw new BusinessException("No se ha podido realizar la consulta.");
		}

		return resultDto;
	}

	public MicroserviceAttachmentDto createAttachment(Long supplyRequestedId, Long boundaryId, String urlFile,
			Long createdBy) {

		MicroserviceAttachmentDto attachmentDto = null;

		try {

			MicroserviceCreateAttachmentDto createAttachmentDto = new MicroserviceCreateAttachmentDto();
			createAttachmentDto.setBoundaryId(boundaryId);
			createAttachmentDto.setCreatedBy(createdBy);
			createAttachmentDto.setFileUrl(urlFile);

			attachmentDto = providerClient.createAttachment(supplyRequestedId, createAttachmentDto);
		} catch (Exception e) {
			log.error("No se ha podido crear el anexo: " + e.getMessage());
		}

		return attachmentDto;
	}

	public void uploadAttachmentToRevision(MicroserviceProviderDto prodiverDto, MultipartFile fileUploaded,
			Long supplyRequestedId, Long boundaryId, Long userCode) throws BusinessException {

		MicroserviceSupplyRequestedDto supplyRequestedDto = this.getSupplyRequestedById(supplyRequestedId);
		if (supplyRequestedDto == null) {
			throw new BusinessException("El insumo solicitado no existe");
		}

		if (!supplyRequestedDto.getTypeSupply().getProvider().getId().equals(prodiverDto.getId())) {
			throw new BusinessException("El insumo solicitado no pertenece al proveedor");
		}

		if (!supplyRequestedDto.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_IN_REVIEW)) {
			throw new BusinessException(
					"El estado en el que se encuentra el insumo no permite actualizar el registro.");
		}

		MicroserviceSupplyRevisionDto supplyRevisionDto = getSupplyRevisionFromSupplyRequested(supplyRequestedId);
		if (supplyRevisionDto == null) {
			throw new BusinessException("No existe una revisión para el insumo.");
		}

		// save file
		String urlBase = "/anexos/" + supplyRequestedId;
		urlBase = FileTool.removeAccents(urlBase);
		String urlFile = fileBusiness.saveFileToSystem(fileUploaded, urlBase, true);

		String FTPFileName = boundaryId + ".zip";

		String usernameFTP = null;
		String passwordFTP = null;
		try {
			usernameFTP = cryptoBusiness.decrypt(supplyRevisionDto.getUsername());
			passwordFTP = cryptoBusiness.decrypt(supplyRevisionDto.getPassword());
		} catch (Exception e) {
			log.error("Error consultando datos de conexión al FTP:" + e.getMessage());
		}

		Boolean uploadFilToFTPServer = ftpBusiness.uploadFileToFTP(urlFile, FTPFileName, usernameFTP, passwordFTP);
		if (!uploadFilToFTPServer) {
			throw new BusinessException("No se ha podido cargar el archivo al servidor FTP.");
		}

		try {

			String host = cryptoBusiness.decrypt(supplyRevisionDto.getHostname());
			String database = cryptoBusiness.decrypt(supplyRevisionDto.getDatabase());
			String password = databaseIntegrationPassword;
			String port = cryptoBusiness.decrypt(supplyRevisionDto.getPort());
			String schema = cryptoBusiness.decrypt(supplyRevisionDto.getSchema());
			String username = databaseIntegrationUsername;

			String namespace = supplyRequestedDto.getTypeSupply().getProvider().getName() + "_FUENTECABIDALINDEROS";

			iliBusiness.updateRecordFromRevision(host, database, password, port, schema, username,
					supplyRequestedDto.getModelVersion(), boundaryId, ProviderBusiness.PROVIDER_SNR_ID, namespace,
					FTPFileName);

		} catch (Exception e) {
			log.error("No se ha podido realizar la consulta: " + e.getMessage());
			throw new BusinessException("No se ha podido actualizar el registro.");
		}

		MicroserviceAttachmentDto attachmentDto = createAttachment(supplyRequestedId, boundaryId, urlFile, userCode);
		if (attachmentDto == null) {
			throw new BusinessException("No se ha podido actualizar el registro.");
		}

		// delete file
		try {
			FileUtils.deleteQuietly(new File(urlFile));
		} catch (Exception e) {
			log.error("No se ha podido eliminar el archivo temporal: " + e.getMessage());
		}

	}

	public void closeRevision(Long supplyRequestedId, Long userCode, MicroserviceProviderDto prodiverDto)
			throws BusinessException {

		MicroserviceSupplyRequestedDto supplyRequestedDto = this.getSupplyRequestedById(supplyRequestedId);
		if (supplyRequestedDto == null) {
			throw new BusinessException("El insumo solicitado no existe");
		}

		if (!supplyRequestedDto.getTypeSupply().getProvider().getId().equals(prodiverDto.getId())) {
			throw new BusinessException("El insumo solicitado no pertenece al proveedor");
		}

		if (!supplyRequestedDto.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_IN_REVIEW)) {
			throw new BusinessException("El estado en el que se encuentra el insumo no permite cerrar una revisión.");
		}

		MicroserviceSupplyRevisionDto supplyRevisionDto = getSupplyRevisionFromSupplyRequested(supplyRequestedId);
		if (supplyRevisionDto == null) {
			throw new BusinessException("No existe una revisión para el insumo.");
		}

		// start export
		try {

			String host = cryptoBusiness.decrypt(supplyRevisionDto.getHostname());
			String database = cryptoBusiness.decrypt(supplyRevisionDto.getDatabase());
			String password = databaseIntegrationPassword;
			String port = cryptoBusiness.decrypt(supplyRevisionDto.getPort());
			String schema = cryptoBusiness.decrypt(supplyRevisionDto.getSchema());
			String username = databaseIntegrationUsername;

			String reference = "export-" + supplyRequestedDto.getId() + "-" + supplyRequestedDto.getRequest().getId()
					+ "-" + userCode;

			String randomFilename = RandomStringUtils.random(20, true, false).toLowerCase();
			String pathFile = stFilesDirectory + "/anexos/" + supplyRequestedId + File.separator + randomFilename
					+ ".xtf";

			iliBusiness.startExportReference(pathFile, host, database, password, port, schema, username, reference,
					supplyRequestedDto.getModelVersion().trim(), IliBusiness.ILI_CONCEPT_INTEGRATION);

		} catch (Exception e) {
			log.error("No se ha podido iniciar la exportación: " + e.getMessage());
			throw new BusinessException("No se ha podido iniciar la exportación.");
		}

		// update state supply requested to Closing Revision
		updateStateToSupplyRequested(supplyRequestedDto.getRequest().getId(), supplyRequestedId,
				ProviderBusiness.SUPPLY_REQUESTED_STATE_CLOSING_REVIEW);

	}

	public MicroserviceRequestDto getRequestById(Long requestId) {

		MicroserviceRequestDto requestDto = null;

		try {
			requestDto = providerClient.findRequestById(requestId);
		} catch (Exception e) {
			log.error("Error consultando solicitud por id: " + e.getMessage());
		}

		return requestDto;
	}

	public MicroserviceSupplyRevisionDto updateSupplyRevision(Long supplyRequestedId, Long supplyRevisionId,
			MicroserviceUpdateSupplyRevisionDto updateData) {

		MicroserviceSupplyRevisionDto supplyRevisionDto = null;

		try {

			supplyRevisionDto = providerClient.updateSupplyRevision(supplyRequestedId, supplyRevisionId, updateData);

		} catch (Exception e) {
			log.error("No se ha podido actualizar la revisión: " + e.getMessage());
		}

		return supplyRevisionDto;
	}

}
