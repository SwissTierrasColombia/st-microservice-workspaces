package com.ai.st.microservice.workspaces.business;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.clients.TaskFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyAttachmentDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCancelTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskPropertyDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskStepDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskCategoryDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMemberDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataPropertyDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IIntegrationService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
public class TaskBusiness {

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

	private final Logger log = LoggerFactory.getLogger(TaskBusiness.class);

	public static final Long TASK_CATEGORY_INTEGRATION = (long) 1;
	public static final Long TASK_CATEGORY_CADASTRAL_INPUT_GENERATION = (long) 2;

	public static final Long TASK_TYPE_STEP_ONCE = (long) 1;
	public static final Long TASK_TYPE_STEP_ALWAYS = (long) 2;

	public static final Long TASK_STATE_ASSIGNED = (long) 1;
	public static final Long TASK_STATE_CLOSED = (long) 2;
	public static final Long TASK_STATE_CANCELLED = (long) 3;
	public static final Long TASK_STATE_STARTED = (long) 4;

	@Autowired
	private TaskFeignClient taskClient;

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private SupplyFeignClient supplyClient;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private IliBusiness iliBusiness;

	@Autowired
	private CrytpoBusiness cryptoBusiness;

	@Autowired
	private IntegrationBusiness integrationBusiness;

	@Autowired
	private DatabaseIntegrationBusiness databaseIntegrationBusiness;

	@Autowired
	private ProviderBusiness providerBusiness;

	@Autowired
	private IIntegrationService integrationService;

	public MicroserviceTaskDto extendTask(MicroserviceTaskDto taskDto) {

		List<MicroserviceTaskMemberDto> members = new ArrayList<MicroserviceTaskMemberDto>();
		for (MicroserviceTaskMemberDto member : taskDto.getMembers()) {
			try {
				MicroserviceUserDto userDto = userClient.findById(member.getMemberCode());
				member.setUser(userDto);
			} catch (Exception e) {
				member.setUser(null);
			}
			members.add(member);
		}
		taskDto.setMembers(members);

		JsonObject objectMetadata = new JsonObject();

		for (MicroserviceTaskMetadataDto metadataDto : taskDto.getMetadata()) {

			JsonObject objectProperties = new JsonObject();
			for (MicroserviceTaskMetadataPropertyDto propertyDto : metadataDto.getProperties()) {
				objectProperties.addProperty(propertyDto.getKey(), propertyDto.getValue());
			}

			objectMetadata.add(metadataDto.getKey(), objectProperties);

		}

		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		Map<String, Object> mapData = gson.fromJson(objectMetadata.toString(), Map.class);
		taskDto.setData(mapData);

		return taskDto;
	}

	public List<MicroserviceTaskDto> getPendingTasks(Long userCode) throws BusinessException {

		List<MicroserviceTaskDto> listTasksDto = new ArrayList<MicroserviceTaskDto>();

		try {

			List<Long> taskStates = new ArrayList<>();
			taskStates.add(TaskBusiness.TASK_STATE_ASSIGNED);
			taskStates.add(TaskBusiness.TASK_STATE_STARTED);

			List<MicroserviceTaskDto> listResponseTasks = taskClient.findByUserAndState(userCode, taskStates);

			for (MicroserviceTaskDto taskDto : listResponseTasks) {
				taskDto = this.extendTask(taskDto);
				listTasksDto.add(taskDto);
			}

		} catch (Exception e) {
			throw new BusinessException("No se ha podido consultar las tareas pendientes del usuario.");
		}

		return listTasksDto;
	}

	public MicroserviceTaskDto createTask(List<Long> categories, String deadline, String description, String name,
			List<Long> users, List<MicroserviceCreateTaskMetadataDto> metadata,
			List<MicroserviceCreateTaskStepDto> steps) throws BusinessException {

		MicroserviceTaskDto taskDto = null;

		try {

			MicroserviceCreateTaskDto createTask = new MicroserviceCreateTaskDto();
			createTask.setCategories(categories);
			createTask.setDeadline(deadline);
			createTask.setDescription(description);
			createTask.setMetadata(metadata);
			createTask.setName(name);
			createTask.setUsers(users);
			createTask.setSteps(steps);

			taskDto = taskClient.createTask(createTask);

		} catch (Exception e) {
			log.error("No se ha podido crear la tarea: " + e.getMessage());
			throw new BusinessException("No se ha podido crear la tarea.");
		}

		return taskDto;
	}

	public MicroserviceTaskDto createTaskForGenerationSupply(List<Long> users, String municipality, Long requestId,
			Long typeSupplyId, Date dateDeadline, String modelVersion) throws BusinessException {

		List<Long> taskCategories = new ArrayList<>();
		taskCategories.add(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String deadline = "";
		if (dateDeadline == null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 30);
			deadline = sdf.format(cal.getTime());
		} else {
			deadline = sdf.format(dateDeadline.getTime());
		}

		String description = "Generación de insumo catastral para el municipio " + municipality;
		String name = "Generar insumo catastral " + municipality;

		List<MicroserviceCreateTaskMetadataDto> metadata = new ArrayList<>();

		MicroserviceCreateTaskMetadataDto metadataRequest = new MicroserviceCreateTaskMetadataDto();
		metadataRequest.setKey("request");
		List<MicroserviceCreateTaskPropertyDto> listPropertiesRequest = new ArrayList<>();
		listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("requestId", requestId.toString()));
		listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("typeSupplyId", typeSupplyId.toString()));
		listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("municipality", municipality));
		listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("modelVersion", modelVersion));

		metadataRequest.setProperties(listPropertiesRequest);
		metadata.add(metadataRequest);

		List<MicroserviceCreateTaskStepDto> steps = new ArrayList<>();

		return this.createTask(taskCategories, deadline, description, name, users, metadata, steps);
	}

	public MicroserviceTaskDto startTask(Long taskId, Long userId) throws BusinessException {

		MicroserviceTaskDto taskDto = null;

		try {
			taskDto = taskClient.findTaskById(taskId);
		} catch (Exception e) {
			throw new BusinessException("No se ha encontrado la tarea.");
		}

		// verify state
		if (!taskDto.getTaskState().getId().equals(TaskBusiness.TASK_STATE_ASSIGNED)) {
			throw new BusinessException("No se puede iniciar la tarea porque no esta en estado asignada.");
		}

		// verify if the user is assigned the task
		MicroserviceTaskMemberDto memberFound = taskDto.getMembers().stream()
				.filter(memberDto -> memberDto.getMemberCode().equals(userId)).findAny().orElse(null);

		if (!(memberFound instanceof MicroserviceTaskMemberDto)) {
			throw new BusinessException("El usuario no tiene asignada la tarea.");
		}

		MicroserviceTaskCategoryDto categoryFound = taskDto.getCategories().stream()
				.filter(categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_INTEGRATION)
						|| categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION))
				.findAny().orElse(null);

		// task of integration
		if (categoryFound instanceof MicroserviceTaskCategoryDto) {
			try {
				for (MicroserviceTaskMemberDto memberDto : taskDto.getMembers()) {
					if (!memberDto.getMemberCode().equals(userId)) {
						taskClient.removeMemberFromTask(taskId, memberDto.getMemberCode());
					}
				}
			} catch (Exception e) {
				log.error("No se ha podido desasignar los usuarios de la tarea: " + e.getMessage());
			}
		}

		try {
			taskDto = taskClient.startTask(taskId);
			taskDto = this.extendTask(taskDto);
		} catch (Exception e) {
			log.error("No se ha podido iniciar la tarea: " + e.getMessage());
			throw new BusinessException("No se ha podido iniciar la tarea.");
		}

		return taskDto;
	}

	public MicroserviceTaskDto finishTask(Long taskId, MicroserviceUserDto userDto) throws BusinessException {

		MicroserviceTaskDto taskDto = null;

		try {
			taskDto = taskClient.findTaskById(taskId);
		} catch (Exception e) {
			throw new BusinessException("No se ha encontrado la tarea.");
		}

		// verify state
		if (!taskDto.getTaskState().getId().equals(TaskBusiness.TASK_STATE_STARTED)) {
			throw new BusinessException("No se puede iniciar la tarea porque no esta en estado iniciada.");
		}

		// verify if the user is assigned the task
		MicroserviceTaskMemberDto memberFound = taskDto.getMembers().stream()
				.filter(memberDto -> memberDto.getMemberCode().equals(userDto.getId())).findAny().orElse(null);

		if (!(memberFound instanceof MicroserviceTaskMemberDto)) {
			throw new BusinessException("El usuario no tiene asignada la tarea.");
		}

		MicroserviceTaskCategoryDto categoryIntegrationFound = taskDto.getCategories().stream()
				.filter(categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_INTEGRATION)).findAny()
				.orElse(null);

		// task of integration
		try {

			if (categoryIntegrationFound instanceof MicroserviceTaskCategoryDto) {

				MicroserviceTaskMetadataDto metadataIntegration = taskDto.getMetadata().stream()
						.filter(metadataDto -> metadataDto.getKey().equals("integration")).findAny().orElse(null);
				if (metadataIntegration instanceof MicroserviceTaskMetadataDto) {

					MicroserviceTaskMetadataPropertyDto propertyIntegration = metadataIntegration.getProperties()
							.stream().filter(propertyDto -> propertyDto.getKey().equals("integration")).findAny()
							.orElse(null);
					if (propertyIntegration instanceof MicroserviceTaskMetadataPropertyDto) {
						Long integrationId = Long.parseLong(propertyIntegration.getValue());

						IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
						if (integrationEntity instanceof IntegrationEntity) {

							// modify integration state to finish assisted
							String textHistory = userDto.getFirstName() + " " + userDto.getLastName();
							integrationBusiness.updateStateToIntegration(integrationId,
									IntegrationStateBusiness.STATE_FINISHED_ASSISTED, userDto.getId(), null,
									textHistory);

							String hostnameDecrypt = cryptoBusiness.decrypt(integrationEntity.getHostname());
							String databaseDecrypt = cryptoBusiness.decrypt(integrationEntity.getDatabase());
							String portDecrypt = cryptoBusiness.decrypt(integrationEntity.getPort());
							String schemaDecrypt = cryptoBusiness.decrypt(integrationEntity.getSchema());

							WorkspaceEntity workspaceEntity = integrationEntity.getWorkspace();

							// supply cadastre
							MicroserviceSupplyDto supplyCadastreDto = supplyClient
									.findSupplyById(integrationEntity.getSupplyCadastreId());

							/**
							 * TODO: Refactoring pending ...
							 * 
							 * Before:
							 * 
							 * String urlBase = "/" + workspaceEntity.getMunicipality().getCode().replace("
							 * ", "_") + "/insumos/gestores/" + workspaceEntity.getManagerCode();
							 * 
							 * 
							 */

							String urlBase = "/" + workspaceEntity.getMunicipality().getCode().replace(" ", "_")
									+ "/insumos/gestores/";

							iliBusiness.startExport(hostnameDecrypt, databaseDecrypt, databaseIntegrationPassword,
									portDecrypt, schemaDecrypt, databaseIntegrationUsername, integrationId, true,
									supplyCadastreDto.getModelVersion(), urlBase);

							// modify integration state to generating product
							integrationBusiness.updateStateToIntegration(integrationId,
									IntegrationStateBusiness.STATE_GENERATING_PRODUCT, null, null, "SISTEMA");

						}
					}
				}
			}

		} catch (Exception e) {
			log.error("No se ha podido empezar a generar el producto: " + e.getMessage());
		}

		MicroserviceTaskCategoryDto categoryGenerationFound = taskDto.getCategories().stream().filter(
				categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION))
				.findAny().orElse(null);

		// task for generation of supplies
		if (categoryGenerationFound instanceof MicroserviceTaskCategoryDto) {

			MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
					.filter(metadataDto -> metadataDto.getKey().equals("request")).findAny().orElse(null);

			if (metadataRequest instanceof MicroserviceTaskMetadataDto) {

				MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties().stream()
						.filter(propertyDto -> propertyDto.getKey().equals("requestId")).findAny().orElse(null);

				MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties().stream()
						.filter(propertyDto -> propertyDto.getKey().equals("typeSupplyId")).findAny().orElse(null);

				if (propertyRequest != null && propertyTypeSupply != null) {

					Long requestId = Long.parseLong(propertyRequest.getValue());
					Long typeSuppyId = Long.parseLong(propertyTypeSupply.getValue());

					MicroserviceRequestDto requestDto = providerClient.findRequestById(requestId);
					if (requestDto instanceof MicroserviceRequestDto) {

						MicroserviceSupplyRequestedDto supplyRequestedDto = requestDto.getSuppliesRequested().stream()
								.filter(sR -> sR.getTypeSupply().getId().equals(typeSuppyId)).findAny().orElse(null);
						if (supplyRequestedDto instanceof MicroserviceSupplyRequestedDto) {

							Long supplyStateId = supplyRequestedDto.getState().getId();
							if (supplyStateId.equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_VALIDATING)) {
								throw new BusinessException(
										"No se puede finalizar la tarea, el insumo cargado esta en proceso de validación.");
							}

							if (supplyStateId.equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_REJECTED)) {
								throw new BusinessException(
										"No se puede finalizar la tarea, el insumo cargado ha sido rechazado.");
							}

							if (!supplyStateId.equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED)) {
								throw new BusinessException(
										"No se puede finalizar la tarea, el insumo no ha sido cargado.");
							}
						}
					}
				}
			}
		}

		// close task
		try {
			taskDto = taskClient.closeTask(taskId);
			taskDto = this.extendTask(taskDto);
		} catch (Exception e) {
			log.error("No se ha podido finalizar la tarea: " + e.getMessage());
			throw new BusinessException("No se ha podido finalizar la tarea.");
		}

		return taskDto;
	}

	public MicroserviceTaskDto cancelTask(Long taskId, String reason, MicroserviceUserDto userDto)
			throws BusinessException {

		MicroserviceTaskDto taskDto = null;

		try {
			taskDto = taskClient.findTaskById(taskId);
		} catch (Exception e) {
			throw new BusinessException("No se ha encontrado la tarea.");
		}

		// verify state
		if (!taskDto.getTaskState().getId().equals(TaskBusiness.TASK_STATE_STARTED)) {
			throw new BusinessException("No se puede cancelar la tarea porque no esta en estado iniciada.");
		}

		// verify if the user is assigned the task
		MicroserviceTaskMemberDto memberFound = taskDto.getMembers().stream()
				.filter(memberDto -> memberDto.getMemberCode().equals(userDto.getId())).findAny().orElse(null);

		if (!(memberFound instanceof MicroserviceTaskMemberDto)) {
			throw new BusinessException("El usuario no tiene asignada la tarea.");
		}

		MicroserviceTaskCategoryDto categoryIntegrationFound = taskDto.getCategories().stream()
				.filter(categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_INTEGRATION)).findAny()
				.orElse(null);

		// task of integration
		try {

			if (categoryIntegrationFound instanceof MicroserviceTaskCategoryDto) {

				MicroserviceTaskMetadataDto metadataIntegration = taskDto.getMetadata().stream()
						.filter(metadataDto -> metadataDto.getKey().equals("integration")).findAny().orElse(null);
				if (metadataIntegration instanceof MicroserviceTaskMetadataDto) {

					MicroserviceTaskMetadataPropertyDto propertyIntegration = metadataIntegration.getProperties()
							.stream().filter(propertyDto -> propertyDto.getKey().equals("integration")).findAny()
							.orElse(null);
					if (propertyIntegration instanceof MicroserviceTaskMetadataPropertyDto) {
						Long integrationId = Long.parseLong(propertyIntegration.getValue());

						IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
						if (integrationEntity instanceof IntegrationEntity) {

							// delete database
							try {
								databaseIntegrationBusiness.dropDatabase(
										cryptoBusiness.decrypt(integrationEntity.getDatabase()),
										cryptoBusiness.decrypt(integrationEntity.getUsername()));
							} catch (Exception e) {
								log.error("No se ha podido borrar la base de datos: " + e.getMessage());
							}

							String randomDatabaseName = RandomStringUtils.random(8, true, false).toLowerCase();
							String randomUsername = RandomStringUtils.random(8, true, false).toLowerCase();
							String randomPassword = RandomStringUtils.random(10, true, true);

							// create database
							try {
								databaseIntegrationBusiness.createDatabase(randomDatabaseName, randomUsername,
										randomPassword);
							} catch (Exception e) {
								log.error("No se ha podido crear la base de datos para la integración: "
										+ e.getMessage());
							}

							integrationBusiness.updateCredentialsIntegration(integrationId,
									cryptoBusiness.encrypt(databaseIntegrationHost),
									cryptoBusiness.encrypt(databaseIntegrationPort),
									cryptoBusiness.encrypt(randomDatabaseName),
									cryptoBusiness.encrypt(databaseIntegrationSchema),
									cryptoBusiness.encrypt(randomUsername), cryptoBusiness.encrypt(randomPassword));

							try {

								// file cadastre
								MicroserviceSupplyDto supplyCadastreDto = supplyClient
										.findSupplyById(integrationEntity.getSupplyCadastreId());

								MicroserviceSupplyAttachmentDto attachmentCadastre = supplyCadastreDto.getAttachments()
										.stream()
										.filter(a -> a.getAttachmentType().getId()
												.equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY))
										.findAny().orElse(null);

								// file register
								MicroserviceSupplyDto supplyRegisteredDto = supplyClient
										.findSupplyById(integrationEntity.getSupplySnrId());
								MicroserviceSupplyAttachmentDto attachmentRegister = supplyRegisteredDto
										.getAttachments().stream()
										.filter(a -> a.getAttachmentType().getId()
												.equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY))
										.findAny().orElse(null);

								iliBusiness.startIntegration(attachmentCadastre.getData(), attachmentRegister.getData(),
										databaseIntegrationHost, randomDatabaseName, databaseIntegrationPassword,
										databaseIntegrationPort, databaseIntegrationSchema, databaseIntegrationUsername,
										integrationId, supplyCadastreDto.getModelVersion());

							} catch (Exception e) {
								log.error("No se ha podido iniciar la integración: " + e.getMessage());
							}

							// modify integration state to finish assisted
							String textHistory = userDto.getFirstName() + " " + userDto.getLastName();
							integrationBusiness.updateStateToIntegration(integrationId,
									IntegrationStateBusiness.STATE_STARTED_AUTOMATIC, userDto.getId(), null,
									textHistory);

						}
					}
				}
			}

		} catch (Exception e) {
			log.error("No se ha podido empezar a generar el producto: " + e.getMessage());
		}

		MicroserviceTaskCategoryDto categoryGenerationFound = taskDto.getCategories().stream().filter(
				categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION))
				.findAny().orElse(null);

		// task for generation of supplies
		try {

			if (categoryGenerationFound instanceof MicroserviceTaskCategoryDto) {

				MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
						.filter(metadataDto -> metadataDto.getKey().equals("request")).findAny().orElse(null);

				if (metadataRequest instanceof MicroserviceTaskMetadataDto) {

					MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties().stream()
							.filter(propertyDto -> propertyDto.getKey().equals("requestId")).findAny().orElse(null);

					MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties().stream()
							.filter(propertyDto -> propertyDto.getKey().equals("typeSupplyId")).findAny().orElse(null);

					MicroserviceTaskMetadataPropertyDto propertyMunicipality = metadataRequest.getProperties().stream()
							.filter(propertyDto -> propertyDto.getKey().equals("municipality")).findAny().orElse(null);

					MicroserviceTaskMetadataPropertyDto propertyModelVersion = metadataRequest.getProperties().stream()
							.filter(propertyDto -> propertyDto.getKey().equals("modelVersion")).findAny().orElse(null);

					if (propertyRequest != null && propertyTypeSupply != null && propertyMunicipality != null
							&& propertyModelVersion != null) {

						Long requestId = Long.parseLong(propertyRequest.getValue());
						Long typeSuppyId = Long.parseLong(propertyTypeSupply.getValue());
						String municipality = propertyMunicipality.getValue();

						MicroserviceRequestDto requestDto = providerClient.findRequestById(requestId);
						if (requestDto instanceof MicroserviceRequestDto) {

							List<Long> profiles = new ArrayList<>();
							profiles.add(ProviderBusiness.PROVIDER_PROFILE_CADASTRAL);
							List<MicroserviceProviderUserDto> providerUsersDto = providerBusiness
									.getUsersByProvider(requestDto.getProvider().getId(), profiles);

							List<Long> users = new ArrayList<>();
							for (MicroserviceProviderUserDto providerUserDto : providerUsersDto) {
								users.add(providerUserDto.getUserCode());
							}

							this.createTaskForGenerationSupply(users, municipality, requestId, typeSuppyId,
									taskDto.getDeadline(), propertyModelVersion.getValue());
						}

					}

				}

			}

		} catch (Exception e) {
			log.error("No se ha podido re-asignar la tarea: " + e.getMessage());
		}

		try {
			MicroserviceCancelTaskDto cancelTaskDto = new MicroserviceCancelTaskDto();
			cancelTaskDto.setReason(reason);
			taskDto = taskClient.cancelTask(taskId, cancelTaskDto);
			taskDto = this.extendTask(taskDto);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido cancelar la tarea.");
		}

		return taskDto;
	}

}
