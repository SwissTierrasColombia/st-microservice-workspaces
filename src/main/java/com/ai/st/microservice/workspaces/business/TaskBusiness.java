package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.*;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.common.dto.quality.MicroserviceXTFAttachmentDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceSupplyAttachmentDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.common.dto.tasks.*;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.providers.CustomRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.supplies.CustomSupplyDto;
import com.ai.st.microservice.workspaces.dto.tasks.CustomTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.CustomTaskMemberDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IIntegrationService;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public static final Long TASK_CATEGORY_XTF_QUALITY_PROCESS = (long) 3;

    public static final Long TASK_TYPE_STEP_ONCE = (long) 1;
    public static final Long TASK_TYPE_STEP_ALWAYS = (long) 2;

    public static final Long TASK_STATE_ASSIGNED = (long) 1;
    public static final Long TASK_STATE_CLOSED = (long) 2;
    public static final Long TASK_STATE_CANCELLED = (long) 3;
    public static final Long TASK_STATE_STARTED = (long) 4;

    private final TaskFeignClient taskClient;
    private final UserFeignClient userClient;
    private final SupplyFeignClient supplyClient;
    private final ProviderFeignClient providerClient;
    private final IliBusiness iliBusiness;
    private final CrytpoBusiness cryptoBusiness;
    private final IntegrationBusiness integrationBusiness;
    private final ProviderBusiness providerBusiness;
    private final DatabaseIntegrationBusiness databaseIntegrationBusiness;
    private final IIntegrationService integrationService;
    private final QualityFeignClient qualityClient;

    public TaskBusiness(TaskFeignClient taskClient, UserFeignClient userClient, SupplyFeignClient supplyClient,
            ProviderFeignClient providerClient, IliBusiness iliBusiness, CrytpoBusiness cryptoBusiness,
            IntegrationBusiness integrationBusiness, DatabaseIntegrationBusiness databaseIntegrationBusiness,
            ProviderBusiness providerBusiness, IIntegrationService integrationService,
            QualityFeignClient qualityClient) {
        this.taskClient = taskClient;
        this.userClient = userClient;
        this.supplyClient = supplyClient;
        this.providerClient = providerClient;
        this.iliBusiness = iliBusiness;
        this.cryptoBusiness = cryptoBusiness;
        this.integrationBusiness = integrationBusiness;
        this.databaseIntegrationBusiness = databaseIntegrationBusiness;
        this.providerBusiness = providerBusiness;
        this.integrationService = integrationService;
        this.qualityClient = qualityClient;
    }

    public CustomTaskDto extendTask(CustomTaskDto taskDto) {

        List<CustomTaskMemberDto> members = new ArrayList<>();

        List<? extends MicroserviceTaskMemberDto> response = taskDto.getMembers();
        List<CustomTaskMemberDto> tasksMembersDto = response.stream().map(CustomTaskMemberDto::new)
                .collect(Collectors.toList());

        for (CustomTaskMemberDto member : tasksMembersDto) {
            try {
                MicroserviceUserDto userDto = userClient.findById(member.getMemberCode());
                userDto.setEmail("");
                userDto.setFirstName("");
                userDto.setLastName("");
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

    public List<CustomTaskDto> getPendingTasks(Long userCode) throws BusinessException {

        List<CustomTaskDto> listTasksDto = new ArrayList<>();

        try {

            List<Long> taskStates = new ArrayList<>();
            taskStates.add(TaskBusiness.TASK_STATE_ASSIGNED);
            taskStates.add(TaskBusiness.TASK_STATE_STARTED);

            List<MicroserviceTaskDto> response = taskClient.findByUserAndState(userCode, taskStates);
            List<CustomTaskDto> listResponseTasks = response.stream().map(CustomTaskDto::new)
                    .collect(Collectors.toList());

            for (CustomTaskDto taskDto : listResponseTasks) {
                taskDto = this.extendTask(taskDto);
                listTasksDto.add(taskDto);
            }

        } catch (Exception e) {
            throw new BusinessException("No se ha podido consultar las tareas pendientes del usuario.");
        }

        return listTasksDto;
    }

    public CustomTaskDto createTask(List<Long> categories, String deadline, String description, String name,
            List<Long> users, List<MicroserviceCreateTaskMetadataDto> metadata,
            List<MicroserviceCreateTaskStepDto> steps) throws BusinessException {

        CustomTaskDto taskDto;

        try {

            MicroserviceCreateTaskDto createTask = new MicroserviceCreateTaskDto();
            createTask.setCategories(categories);
            createTask.setDeadline(deadline);
            createTask.setDescription(description);
            createTask.setMetadata(metadata);
            createTask.setName(name);
            createTask.setUsers(users);
            createTask.setSteps(steps);

            MicroserviceTaskDto response = taskClient.createTask(createTask);
            taskDto = new CustomTaskDto(response);

        } catch (Exception e) {
            log.error("No se ha podido crear la tarea: " + e.getMessage());
            throw new BusinessException("No se ha podido crear la tarea.");
        }

        return taskDto;
    }

    public CustomTaskDto createTaskForGenerationSupply(List<Long> users, String municipality, String department,
            Long requestId, Long typeSupplyId, Date dateDeadline, String modelVersion) throws BusinessException {

        List<Long> taskCategories = new ArrayList<>();
        taskCategories.add(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String deadline;
        if (dateDeadline == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 30);
            deadline = sdf.format(cal.getTime());
        } else {
            deadline = sdf.format(dateDeadline.getTime());
        }

        String description = "Generación de insumo catastral para el Municipio";
        String name = String.format("%s (%s)", municipality, department);

        List<MicroserviceCreateTaskMetadataDto> metadata = new ArrayList<>();

        MicroserviceCreateTaskMetadataDto metadataRequest = new MicroserviceCreateTaskMetadataDto();
        metadataRequest.setKey("request");
        List<MicroserviceCreateTaskPropertyDto> listPropertiesRequest = new ArrayList<>();
        listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("requestId", requestId.toString()));
        listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("typeSupplyId", typeSupplyId.toString()));
        listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("municipality", municipality));
        listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("department", department));
        listPropertiesRequest.add(new MicroserviceCreateTaskPropertyDto("modelVersion", modelVersion));

        metadataRequest.setProperties(listPropertiesRequest);
        metadata.add(metadataRequest);

        List<MicroserviceCreateTaskStepDto> steps = new ArrayList<>();

        return this.createTask(taskCategories, deadline, description, name, users, metadata, steps);
    }

    public CustomTaskDto startTask(Long taskId, Long userId) throws BusinessException {

        CustomTaskDto taskDto;

        try {
            MicroserviceTaskDto response = taskClient.findTaskById(taskId);
            taskDto = new CustomTaskDto(response);
        } catch (Exception e) {
            throw new BusinessException("No se ha encontrado la tarea.");
        }

        // verify state
        if (!taskDto.getTaskState().getId().equals(TaskBusiness.TASK_STATE_ASSIGNED)) {
            throw new BusinessException("No se puede iniciar la tarea porque no esta en estado asignada.");
        }

        // verify if the user is assigned the task
        List<? extends MicroserviceTaskMemberDto> responseMembers = taskDto.getMembers();
        List<CustomTaskMemberDto> membersDto = responseMembers.stream().map(CustomTaskMemberDto::new)
                .collect(Collectors.toList());

        CustomTaskMemberDto memberFound = membersDto.stream()
                .filter(memberDto -> memberDto.getMemberCode().equals(userId)).findAny().orElse(null);

        if (memberFound == null) {
            throw new BusinessException("El usuario no tiene asignada la tarea.");
        }

        // remove others members
        try {
            List<? extends MicroserviceTaskMemberDto> responseMembersDto = taskDto.getMembers();
            List<CustomTaskMemberDto> membersListDto = responseMembersDto.stream().map(CustomTaskMemberDto::new)
                    .collect(Collectors.toList());
            for (CustomTaskMemberDto memberDto : membersListDto) {
                if (!memberDto.getMemberCode().equals(userId)) {
                    taskClient.removeMemberFromTask(taskId, memberDto.getMemberCode());
                }
            }
        } catch (Exception e) {
            log.error("No se ha podido desasignar los usuarios de la tarea: " + e.getMessage());
        }

        try {
            MicroserviceTaskDto response = taskClient.startTask(taskId);
            taskDto = new CustomTaskDto(response);
            taskDto = this.extendTask(taskDto);
        } catch (Exception e) {
            log.error("No se ha podido iniciar la tarea: " + e.getMessage());
            throw new BusinessException("No se ha podido iniciar la tarea.");
        }

        return taskDto;
    }

    public CustomTaskDto finishTask(Long taskId, MicroserviceUserDto userDto) throws BusinessException {

        CustomTaskDto taskDto;

        try {
            MicroserviceTaskDto response = taskClient.findTaskById(taskId);
            taskDto = new CustomTaskDto(response);
        } catch (Exception e) {
            throw new BusinessException("No se ha encontrado la tarea.");
        }

        // verify state
        if (!taskDto.getTaskState().getId().equals(TaskBusiness.TASK_STATE_STARTED)) {
            throw new BusinessException("No se puede iniciar la tarea porque no esta en estado iniciada.");
        }

        // verify if the user is assigned the task
        List<? extends MicroserviceTaskMemberDto> responseMembers = taskDto.getMembers();
        List<CustomTaskMemberDto> membersDto = responseMembers.stream().map(CustomTaskMemberDto::new)
                .collect(Collectors.toList());

        CustomTaskMemberDto memberFound = membersDto.stream()
                .filter(memberDto -> memberDto.getMemberCode().equals(userDto.getId())).findAny().orElse(null);

        if (memberFound == null) {
            throw new BusinessException("El usuario no tiene asignada la tarea.");
        }

        MicroserviceTaskCategoryDto categoryIntegrationFound = taskDto.getCategories().stream()
                .filter(categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_INTEGRATION)).findAny()
                .orElse(null);

        // task of integration
        try {

            if (categoryIntegrationFound != null) {

                MicroserviceTaskMetadataDto metadataIntegration = taskDto.getMetadata().stream()
                        .filter(metadataDto -> metadataDto.getKey().equals("integration")).findAny().orElse(null);
                if (metadataIntegration != null) {

                    MicroserviceTaskMetadataPropertyDto propertyIntegration = metadataIntegration.getProperties()
                            .stream().filter(propertyDto -> propertyDto.getKey().equals("integration")).findAny()
                            .orElse(null);
                    if (propertyIntegration != null) {
                        Long integrationId = Long.parseLong(propertyIntegration.getValue());

                        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
                        if (integrationEntity != null) {

                            // modify integration state to finish assisted
                            String textHistory = userDto.getFirstName() + " " + userDto.getLastName();
                            integrationBusiness.updateStateToIntegration(integrationId,
                                    IntegrationStateBusiness.STATE_FINISHED_ASSISTED, null, userDto.getId(), null,
                                    textHistory);

                            String hostnameDecrypt = cryptoBusiness.decrypt(integrationEntity.getHostname());
                            String databaseDecrypt = cryptoBusiness.decrypt(integrationEntity.getDatabase());
                            String portDecrypt = cryptoBusiness.decrypt(integrationEntity.getPort());
                            String schemaDecrypt = cryptoBusiness.decrypt(integrationEntity.getSchema());

                            WorkspaceEntity workspaceEntity = integrationEntity.getWorkspace();

                            // supply cadastre
                            MicroserviceSupplyDto response = supplyClient
                                    .findSupplyById(integrationEntity.getSupplyCadastreId());
                            CustomSupplyDto supplyCadastreDto = new CustomSupplyDto(response);

                            String urlBase = "/" + workspaceEntity.getMunicipality().getCode().replace(" ", "_")
                                    + "/insumos/gestores/" + integrationEntity.getManagerCode();

                            iliBusiness.startExport(hostnameDecrypt, databaseDecrypt, databaseIntegrationPassword,
                                    portDecrypt, schemaDecrypt, databaseIntegrationUsername, integrationId, true,
                                    supplyCadastreDto.getModelVersion(), urlBase);

                            // modify integration state to generating product
                            integrationBusiness.updateStateToIntegration(integrationId,
                                    IntegrationStateBusiness.STATE_GENERATING_PRODUCT, null, null, null, "SISTEMA");

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
        if (categoryGenerationFound != null) {

            MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
                    .filter(metadataDto -> metadataDto.getKey().equals("request")).findAny().orElse(null);

            if (metadataRequest != null) {

                MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties().stream()
                        .filter(propertyDto -> propertyDto.getKey().equals("requestId")).findAny().orElse(null);

                MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties().stream()
                        .filter(propertyDto -> propertyDto.getKey().equals("typeSupplyId")).findAny().orElse(null);

                if (propertyRequest != null && propertyTypeSupply != null) {

                    Long requestId = Long.parseLong(propertyRequest.getValue());
                    Long typeSupplyId = Long.parseLong(propertyTypeSupply.getValue());

                    MicroserviceRequestDto response = providerClient.findRequestById(requestId);

                    if (response != null) {

                        CustomRequestDto requestDto = new CustomRequestDto(response);

                        List<? extends MicroserviceSupplyRequestedDto> suppliesResponse = requestDto
                                .getSuppliesRequested();
                        List<CustomSupplyRequestedDto> suppliesRequestDto = suppliesResponse.stream()
                                .map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

                        CustomSupplyRequestedDto supplyRequestedDto = suppliesRequestDto.stream()
                                .filter(sR -> sR.getTypeSupply().getId().equals(typeSupplyId)).findAny().orElse(null);

                        if (supplyRequestedDto instanceof CustomSupplyRequestedDto) {

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

        MicroserviceTaskCategoryDto categoryXTFQuality = taskDto.getCategories().stream()
                .filter(categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_XTF_QUALITY_PROCESS))
                .findAny().orElse(null);

        if (categoryXTFQuality != null) {

            MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
                    .filter(meta -> meta.getKey().equalsIgnoreCase("attachment")).findAny().orElse(null);
            if (metadataRequest != null) {

                BusinessException error = new BusinessException(
                        "Ha ocurrido un error finalizando la tarea del proceso de calidad");

                MicroserviceTaskMetadataPropertyDto propertyDeliveryId = metadataRequest.getProperties().stream()
                        .filter(p -> p.getKey().equalsIgnoreCase("deliveryId")).findAny().orElseThrow(() -> error);
                MicroserviceTaskMetadataPropertyDto propertyDeliveryProductId = metadataRequest.getProperties().stream()
                        .filter(p -> p.getKey().equalsIgnoreCase("deliveryProductId")).findAny()
                        .orElseThrow(() -> error);
                MicroserviceTaskMetadataPropertyDto propertyAttachmentId = metadataRequest.getProperties().stream()
                        .filter(p -> p.getKey().equalsIgnoreCase("attachmentId")).findAny().orElseThrow(() -> error);

                Long deliveryId = Long.parseLong(propertyDeliveryId.getValue());
                Long productId = Long.parseLong(propertyDeliveryProductId.getValue());
                Long attachmentId = Long.parseLong(propertyAttachmentId.getValue());

                MicroserviceXTFAttachmentDto attachmentDto = qualityClient.findAttachmentById(deliveryId, productId,
                        attachmentId);

                if (!attachmentDto.getData().isHasReportRevision()) {
                    throw new BusinessException(
                            "No se puede finalizar la tarea porque no se ha cargado el reporte de revisión al archivo XTF");
                }

                qualityClient.udpdateXTFStatusToQualityProcessFinished(deliveryId, productId, attachmentId);
            }
        }

        // close task
        try {
            MicroserviceTaskDto response = taskClient.closeTask(taskId);
            taskDto = new CustomTaskDto(response);
            taskDto = this.extendTask(taskDto);
        } catch (Exception e) {
            log.error("No se ha podido finalizar la tarea: " + e.getMessage());
            throw new BusinessException("No se ha podido finalizar la tarea.");
        }

        return taskDto;
    }

    public CustomTaskDto cancelTask(Long taskId, String reason, MicroserviceUserDto userDto) throws BusinessException {

        CustomTaskDto taskDto;

        try {
            MicroserviceTaskDto response = taskClient.findTaskById(taskId);
            taskDto = new CustomTaskDto(response);
        } catch (Exception e) {
            throw new BusinessException("No se ha encontrado la tarea.");
        }

        // verify state
        if (!taskDto.getTaskState().getId().equals(TaskBusiness.TASK_STATE_STARTED)) {
            throw new BusinessException("No se puede cancelar la tarea porque no esta en estado iniciada.");
        }

        // verify if the user is assigned the task

        List<? extends MicroserviceTaskMemberDto> responseMembers = taskDto.getMembers();
        List<CustomTaskMemberDto> membersDto = responseMembers.stream().map(CustomTaskMemberDto::new)
                .collect(Collectors.toList());

        CustomTaskMemberDto memberFound = membersDto.stream()
                .filter(memberDto -> memberDto.getMemberCode().equals(userDto.getId())).findAny().orElse(null);

        if (memberFound == null) {
            throw new BusinessException("El usuario no tiene asignada la tarea.");
        }

        MicroserviceTaskCategoryDto categoryIntegrationFound = taskDto.getCategories().stream()
                .filter(categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_INTEGRATION)).findAny()
                .orElse(null);

        // task of integration
        try {

            if (categoryIntegrationFound != null) {

                MicroserviceTaskMetadataDto metadataIntegration = taskDto.getMetadata().stream()
                        .filter(metadataDto -> metadataDto.getKey().equals("integration")).findAny().orElse(null);
                if (metadataIntegration != null) {

                    MicroserviceTaskMetadataPropertyDto propertyIntegration = metadataIntegration.getProperties()
                            .stream().filter(propertyDto -> propertyDto.getKey().equals("integration")).findAny()
                            .orElse(null);
                    if (propertyIntegration != null) {
                        Long integrationId = Long.parseLong(propertyIntegration.getValue());

                        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
                        if (integrationEntity != null) {

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
                                MicroserviceSupplyDto responseCadastre = supplyClient
                                        .findSupplyById(integrationEntity.getSupplyCadastreId());
                                CustomSupplyDto supplyCadastreDto = new CustomSupplyDto(responseCadastre);

                                MicroserviceSupplyAttachmentDto attachmentCadastre = supplyCadastreDto.getAttachments()
                                        .stream()
                                        .filter(a -> a.getAttachmentType().getId()
                                                .equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY))
                                        .findAny().orElse(null);

                                // file register
                                MicroserviceSupplyDto responseSnr = supplyClient
                                        .findSupplyById(integrationEntity.getSupplySnrId());
                                CustomSupplyDto supplyRegisteredDto = new CustomSupplyDto(responseSnr);
                                MicroserviceSupplyAttachmentDto attachmentRegister = supplyRegisteredDto
                                        .getAttachments().stream()
                                        .filter(a -> a.getAttachmentType().getId()
                                                .equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY))
                                        .findAny().orElse(null);

                                assert attachmentCadastre != null;
                                assert attachmentRegister != null;
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
                                    IntegrationStateBusiness.STATE_STARTED_AUTOMATIC, null, userDto.getId(), null,
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

            if (categoryGenerationFound != null) {

                MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
                        .filter(metadataDto -> metadataDto.getKey().equals("request")).findAny().orElse(null);

                if (metadataRequest != null) {

                    MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties().stream()
                            .filter(propertyDto -> propertyDto.getKey().equals("requestId")).findAny().orElse(null);

                    MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties().stream()
                            .filter(propertyDto -> propertyDto.getKey().equals("typeSupplyId")).findAny().orElse(null);

                    MicroserviceTaskMetadataPropertyDto propertyMunicipality = metadataRequest.getProperties().stream()
                            .filter(propertyDto -> propertyDto.getKey().equals("municipality")).findAny().orElse(null);

                    MicroserviceTaskMetadataPropertyDto propertyDepartment = metadataRequest.getProperties().stream()
                            .filter(propertyDto -> propertyDto.getKey().equals("department")).findAny().orElse(null);

                    MicroserviceTaskMetadataPropertyDto propertyModelVersion = metadataRequest.getProperties().stream()
                            .filter(propertyDto -> propertyDto.getKey().equals("modelVersion")).findAny().orElse(null);

                    if (propertyRequest != null && propertyTypeSupply != null && propertyMunicipality != null
                            && propertyModelVersion != null && propertyDepartment != null) {

                        Long requestId = Long.parseLong(propertyRequest.getValue());
                        Long typeSupplyId = Long.parseLong(propertyTypeSupply.getValue());
                        String municipality = propertyMunicipality.getValue();
                        String department = propertyDepartment.getValue();

                        MicroserviceRequestDto response = providerClient.findRequestById(requestId);

                        if (response != null) {

                            CustomRequestDto requestDto = new CustomRequestDto(response);

                            List<Long> profiles = new ArrayList<>();
                            profiles.add(ProviderBusiness.PROVIDER_PROFILE_CADASTRAL);
                            List<MicroserviceProviderUserDto> providerUsersDto = providerBusiness
                                    .getUsersByProvider(requestDto.getProvider().getId(), profiles);

                            List<Long> users = new ArrayList<>();
                            for (MicroserviceProviderUserDto providerUserDto : providerUsersDto) {
                                users.add(providerUserDto.getUserCode());
                            }

                            this.createTaskForGenerationSupply(users, municipality, department, requestId, typeSupplyId,
                                    taskDto.getDeadline(), propertyModelVersion.getValue());
                        }

                    }

                }

            }

        } catch (Exception e) {
            log.error("No se ha podido re-asignar la tarea: " + e.getMessage());
        }

        MicroserviceTaskCategoryDto categoryXTFQuality = taskDto.getCategories().stream()
                .filter(categoryDto -> categoryDto.getId().equals(TaskBusiness.TASK_CATEGORY_XTF_QUALITY_PROCESS))
                .findAny().orElse(null);

        if (categoryXTFQuality != null) {

            MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
                    .filter(meta -> meta.getKey().equalsIgnoreCase("attachment")).findAny().orElse(null);
            if (metadataRequest != null) {

                BusinessException error = new BusinessException(
                        "Ha ocurrido un error cancelando la tarea del proceso de calidad");

                MicroserviceTaskMetadataPropertyDto propertyDeliveryId = metadataRequest.getProperties().stream()
                        .filter(p -> p.getKey().equalsIgnoreCase("deliveryId")).findAny().orElseThrow(() -> error);
                MicroserviceTaskMetadataPropertyDto propertyDeliveryProductId = metadataRequest.getProperties().stream()
                        .filter(p -> p.getKey().equalsIgnoreCase("deliveryProductId")).findAny()
                        .orElseThrow(() -> error);
                MicroserviceTaskMetadataPropertyDto propertyAttachmentId = metadataRequest.getProperties().stream()
                        .filter(p -> p.getKey().equalsIgnoreCase("attachmentId")).findAny().orElseThrow(() -> error);

                Long deliveryId = Long.parseLong(propertyDeliveryId.getValue());
                Long productId = Long.parseLong(propertyDeliveryProductId.getValue());
                Long attachmentId = Long.parseLong(propertyAttachmentId.getValue());

                qualityClient.updateXTFStatusToAccepted(deliveryId, productId, attachmentId);
            }

        }

        try {
            MicroserviceCancelTaskDto cancelTaskDto = new MicroserviceCancelTaskDto();
            cancelTaskDto.setReason(reason);

            MicroserviceTaskDto response = taskClient.cancelTask(taskId, cancelTaskDto);
            taskDto = new CustomTaskDto(response);

            taskDto = this.extendTask(taskDto);
        } catch (Exception e) {
            throw new BusinessException("No se ha podido cancelar la tarea.");
        }

        return taskDto;
    }

}
