package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.clients.TaskFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyAttachmentDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskStepDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskCategoryDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMemberDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataPropertyDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
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

	private final Logger log = LoggerFactory.getLogger(TaskBusiness.class);

	public static final Long TASK_CATEGORY_INTEGRATION = (long) 1;

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
	private IliBusiness iliBusiness;

	@Autowired
	private CrytpoBusiness cryptoBusiness;

	@Autowired
	private IntegrationBusiness integrationBusiness;

	@Autowired
	private DatabaseIntegrationBusiness databaseIntegrationBusiness;

	@Autowired
	private IIntegrationService integrationService;

	public List<MicroserviceTaskDto> getPendingTasks(Long userCode) throws BusinessException {

		List<MicroserviceTaskDto> listTasksDto = new ArrayList<MicroserviceTaskDto>();

		try {

			List<Long> taskStates = new ArrayList<>();
			taskStates.add(TaskBusiness.TASK_STATE_ASSIGNED);
			taskStates.add(TaskBusiness.TASK_STATE_STARTED);

			List<MicroserviceTaskDto> listResponseTasks = taskClient.findByUserAndState(userCode, taskStates);

			for (MicroserviceTaskDto taskDto : listResponseTasks) {

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
			throw new BusinessException("No se ha podido crear la tarea.");
		}

		return taskDto;
	}

	public MicroserviceTaskDto startTask(Long taskId, Long userId) throws BusinessException {

		MicroserviceTaskDto taskDto = null;

		try {
			taskDto = taskClient.findTaskById(taskId);
		} catch (Exception e) {
			throw new BusinessException("No se ha encontrado la tarea.");
		}

		// verify state
		if (taskDto.getTaskState().getId() != TaskBusiness.TASK_STATE_ASSIGNED) {
			throw new BusinessException("No se puede iniciar la tarea porque no esta en estado asignada.");
		}

		// verify if the user is assigned the task
		MicroserviceTaskMemberDto memberFound = taskDto.getMembers().stream()
				.filter(memberDto -> memberDto.getMemberCode() == userId).findAny().orElse(null);

		if (!(memberFound instanceof MicroserviceTaskMemberDto)) {
			throw new BusinessException("El usuario no tiene asignada la tarea.");
		}

		MicroserviceTaskCategoryDto categoryFound = taskDto.getCategories().stream()
				.filter(categoryDto -> categoryDto.getId() == TaskBusiness.TASK_CATEGORY_INTEGRATION).findAny()
				.orElse(null);

		// task of integration
		if (categoryFound instanceof MicroserviceTaskCategoryDto) {
			try {
				for (MicroserviceTaskMemberDto memberDto : taskDto.getMembers()) {
					if (memberDto.getMemberCode() != userId) {
						taskClient.removeMemberFromTask(taskId, memberDto.getMemberCode());
					}
				}
			} catch (Exception e) {
				log.error("No se ha podido desasignar los usuarios de la tarea: " + e.getMessage());
			}
		}

		try {
			taskDto = taskClient.startTask(taskId);
		} catch (Exception e) {
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
		if (taskDto.getTaskState().getId() != TaskBusiness.TASK_STATE_STARTED) {
			throw new BusinessException("No se puede iniciar la tarea porque no esta en estado iniciada.");
		}

		// verify if the user is assigned the task
		MicroserviceTaskMemberDto memberFound = taskDto.getMembers().stream()
				.filter(memberDto -> memberDto.getMemberCode() == userDto.getId()).findAny().orElse(null);

		if (!(memberFound instanceof MicroserviceTaskMemberDto)) {
			throw new BusinessException("El usuario no tiene asignada la tarea.");
		}

		MicroserviceTaskCategoryDto categoryFound = taskDto.getCategories().stream()
				.filter(categoryDto -> categoryDto.getId() == TaskBusiness.TASK_CATEGORY_INTEGRATION).findAny()
				.orElse(null);

		// task of integration
		try {

			if (categoryFound instanceof MicroserviceTaskCategoryDto) {

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
							String passwordDecrypt = cryptoBusiness.decrypt(integrationEntity.getPassword());
							String portDecrypt = cryptoBusiness.decrypt(integrationEntity.getPort());
							String schemaDecrypt = cryptoBusiness.decrypt(integrationEntity.getSchema());
							String usernameDecrypt = cryptoBusiness.decrypt(integrationEntity.getUsername());

							iliBusiness.startExport(hostnameDecrypt, databaseDecrypt, passwordDecrypt, portDecrypt,
									schemaDecrypt, usernameDecrypt, integrationId);

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

		try {
			taskDto = taskClient.closeTask(taskId);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido finalizar la tarea.");
		}

		return taskDto;
	}

	public MicroserviceTaskDto cancelTask(Long taskId, MicroserviceUserDto userDto) throws BusinessException {

		MicroserviceTaskDto taskDto = null;

		try {
			taskDto = taskClient.findTaskById(taskId);
		} catch (Exception e) {
			throw new BusinessException("No se ha encontrado la tarea.");
		}

		// verify state
		if (taskDto.getTaskState().getId() != TaskBusiness.TASK_STATE_STARTED) {
			throw new BusinessException("No se puede cancelar la tarea porque no esta en estado iniciada.");
		}

		// verify if the user is assigned the task
		MicroserviceTaskMemberDto memberFound = taskDto.getMembers().stream()
				.filter(memberDto -> memberDto.getMemberCode() == userDto.getId()).findAny().orElse(null);

		if (!(memberFound instanceof MicroserviceTaskMemberDto)) {
			throw new BusinessException("El usuario no tiene asignada la tarea.");
		}

		MicroserviceTaskCategoryDto categoryFound = taskDto.getCategories().stream()
				.filter(categoryDto -> categoryDto.getId() == TaskBusiness.TASK_CATEGORY_INTEGRATION).findAny()
				.orElse(null);

		// task of integration
		try {

			if (categoryFound instanceof MicroserviceTaskCategoryDto) {

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
								databaseIntegrationBusiness
										.dropDatabase(cryptoBusiness.decrypt(integrationEntity.getDatabase()));
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
										.get(0);

								// file register
								MicroserviceSupplyDto supplyRegisteredDto = supplyClient
										.findSupplyById(integrationEntity.getSupplySnrId());
								MicroserviceSupplyAttachmentDto attachmentRegister = supplyRegisteredDto
										.getAttachments().get(0);

								iliBusiness.startIntegration(attachmentCadastre.getUrlDocumentaryRepository(),
										attachmentRegister.getUrlDocumentaryRepository(), databaseIntegrationHost,
										randomDatabaseName, randomPassword, databaseIntegrationPort,
										databaseIntegrationSchema, randomUsername, integrationId);

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

		try {
			taskDto = taskClient.cancelTask(taskId);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido cancelar la tarea.");
		}

		return taskDto;
	}

}
