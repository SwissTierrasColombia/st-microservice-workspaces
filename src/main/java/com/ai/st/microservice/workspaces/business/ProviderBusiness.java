package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.ProviderFeignClient;
import com.ai.st.microservice.common.clients.TaskFeignClient;
import com.ai.st.microservice.common.clients.UserFeignClient;
import com.ai.st.microservice.common.dto.ili.MicroserviceQueryResultRegistralRevisionDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.providers.*;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceCreateSupplyAttachmentDto;
import com.ai.st.microservice.common.dto.tasks.*;
import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.business.RoleBusiness;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomPetitionDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.RequestPackageDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomEmitterDto;
import com.ai.st.microservice.workspaces.dto.tasks.CustomTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.CustomTaskMemberDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
import com.ai.st.microservice.workspaces.utils.FileTool;
import com.ai.st.microservice.workspaces.utils.ZipUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    // Petitions States
    public static final Long PETITION_STATE_PENDING = (long) 1;
    public static final Long PETITION_STATE_ACCEPT = (long) 2;
    public static final Long PETITION_STATE_REJECT = (long) 3;

    // Providers
    public static final Long PROVIDER_IGAC_ID = (long) 1;
    public static final Long PROVIDER_SNR_ID = (long) 8;

    // Profiles
    public static final Long PROVIDER_PROFILE_CADASTRAL = (long) 1;

    // Types supplies
    public static final Long PROVIDER_SUPPLY_CADASTRAL = (long) 2;
    public static final Long PROVIDER_SNR_SUPPLY_REGISTRAL = (long) 12;

    private final Logger log = LoggerFactory.getLogger(ProviderBusiness.class);

    private final ProviderFeignClient providerClient;
    private final TaskFeignClient taskClient;
    private final SupplyBusiness supplyBusiness;
    private final IliBusiness iliBusiness;
    private final FileBusiness fileBusiness;
    private final UserFeignClient userClient;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final IMunicipalityService municipalityService;
    private final DatabaseIntegrationBusiness databaseIntegrationBusiness;
    private final CrytpoBusiness cryptoBusiness;
    private final MunicipalityBusiness municipalityBusiness;
    private final AdministrationBusiness administrationBusiness;

    public ProviderBusiness(ProviderFeignClient providerClient, TaskFeignClient taskClient,
            SupplyBusiness supplyBusiness, IliBusiness iliBusiness, FileBusiness fileBusiness,
            UserFeignClient userClient, ManagerMicroserviceBusiness managerBusiness,
            IMunicipalityService municipalityService, DatabaseIntegrationBusiness databaseIntegrationBusiness,
            CrytpoBusiness cryptoBusiness, MunicipalityBusiness municipalityBusiness,
            AdministrationBusiness administrationBusiness) {
        this.providerClient = providerClient;
        this.taskClient = taskClient;
        this.supplyBusiness = supplyBusiness;
        this.iliBusiness = iliBusiness;
        this.fileBusiness = fileBusiness;
        this.userClient = userClient;
        this.managerBusiness = managerBusiness;
        this.municipalityService = municipalityService;
        this.databaseIntegrationBusiness = databaseIntegrationBusiness;
        this.cryptoBusiness = cryptoBusiness;
        this.municipalityBusiness = municipalityBusiness;
        this.administrationBusiness = administrationBusiness;
    }

    public CustomRequestDto answerRequest(Long requestId, Long typeSupplyId, Boolean skipErrors, String justification,
            MultipartFile[] files, MultipartFile extraFile, String url, MicroserviceProviderDto providerDto,
            Long userCode, String observations) throws BusinessException {

        CustomRequestDto requestUpdatedDto;

        if (files.length == 0 && (url == null || url.isEmpty()) && (justification == null || justification.isEmpty()))
            throw new BusinessException("Se debe justificar porque no se cargará el insumo.");

        MicroserviceRequestDto response = providerClient.findRequestById(requestId);
        CustomRequestDto requestDto = new CustomRequestDto(response);

        if (!providerDto.getId().equals(requestDto.getProvider().getId())) {
            throw new BusinessException("No tiene acceso a la solicitud.");
        }

        if (!requestDto.getRequestState().getId().equals(ProviderBusiness.REQUEST_STATE_REQUESTED)) {
            throw new BusinessException("La solicitud esta cerrada, no se puede modificar.");
        }

        boolean delivered = files.length > 0 || (url != null && !url.isEmpty());
        if (delivered && (observations == null || observations.isEmpty())) {
            throw new BusinessException("Las observaciones son requeridas.");
        }

        List<MicroserviceProviderUserDto> usersByProvider = providerClient.findUsersByProviderId(providerDto.getId());
        MicroserviceProviderUserDto userProviderFound = usersByProvider.stream()
                .filter(user -> userCode.equals(user.getUserCode())).findAny().orElse(null);
        if (userProviderFound == null) {
            throw new BusinessException("El usuario no esta registrado como usuario para el proveedor de insumo.");
        }

        List<? extends MicroserviceSupplyRequestedDto> suppliesResponse = requestDto.getSuppliesRequested();
        List<CustomSupplyRequestedDto> suppliesRequestDto = suppliesResponse.stream().map(CustomSupplyRequestedDto::new)
                .collect(Collectors.toList());

        CustomSupplyRequestedDto supplyRequested = suppliesRequestDto.stream()
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
            List<Long> taskStates = new ArrayList<>(Collections.singletonList(TaskBusiness.TASK_STATE_STARTED));
            List<Long> taskCategories = new ArrayList<>(
                    Collections.singletonList(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION));

            List<MicroserviceTaskDto> responseTasksDto = taskClient.findByStateAndCategory(taskStates, taskCategories);
            List<CustomTaskDto> tasksDto = responseTasksDto.stream().map(CustomTaskDto::new)
                    .collect(Collectors.toList());

            for (CustomTaskDto taskDto : tasksDto) {
                MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
                        .filter(meta -> meta.getKey().equalsIgnoreCase("request")).findAny().orElse(null);
                if (metadataRequest != null) {

                    MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties().stream()
                            .filter(p -> p.getKey().equalsIgnoreCase("requestId")).findAny().orElse(null);

                    MicroserviceTaskMetadataPropertyDto propertyTypeSupply = metadataRequest.getProperties().stream()
                            .filter(p -> p.getKey().equalsIgnoreCase("typeSupplyId")).findAny().orElse(null);

                    if (propertyRequest != null && propertyTypeSupply != null) {

                        Long metaRequestId = Long.parseLong(propertyRequest.getValue());
                        Long metaTypeSupplyId = Long.parseLong(propertyTypeSupply.getValue());

                        if (metaRequestId.equals(requestId) && metaTypeSupplyId.equals(typeSupplyId)) {

                            List<? extends MicroserviceTaskMemberDto> responseMembers = taskDto.getMembers();
                            List<CustomTaskMemberDto> membersDto = responseMembers.stream()
                                    .map(CustomTaskMemberDto::new).collect(Collectors.toList());

                            CustomTaskMemberDto memberDto = membersDto.stream()
                                    .filter(m -> m.getMemberCode().equals(userCode)).findAny().orElse(null);
                            if (memberDto == null) {
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

        String urlExtraFileSaved = null;

        if (delivered) {

            List<String> urls = new ArrayList<>();
            if (files.length > 0) {

                String urlBase = "/" + requestDto.getMunicipalityCode().replace(" ", "_") + "/insumos/proveedores/"
                        + providerDto.getName().replace(" ", "_") + "/"
                        + supplyRequested.getTypeSupply().getName().replace(" ", "_");
                urlBase = FileTool.removeAccents(urlBase);

                if (extraFile != null) {
                    boolean isZip = FilenameUtils.getExtension(extraFile.getOriginalFilename()).equalsIgnoreCase("zip");
                    urlExtraFileSaved = fileBusiness.saveFileToSystem(extraFile, urlBase, !isZip);
                }

                for (MultipartFile fileUploaded : files) {

                    String loadedFileName = fileUploaded.getOriginalFilename();
                    String loadedFileExtension = FilenameUtils.getExtension(loadedFileName);

                    String fileNameRandom = RandomStringUtils.random(14, true, false) + "." + loadedFileExtension;
                    String filePathTemporal = fileBusiness.loadFileToSystem(fileUploaded, fileNameRandom);

                    boolean zipFile = false;

                    List<MicroserviceExtensionDto> extensionAllowed = supplyRequested.getTypeSupply().getExtensions();

                    boolean fileAllowed = false;
                    boolean isLoadShp = false;
                    List<String> loadedFileExtensions = new ArrayList<>();

                    // verify if the supply is a shp file
                    MicroserviceExtensionDto extensionShpDto = extensionAllowed.stream()
                            .filter(ext -> ext.getName().equalsIgnoreCase("shp")).findAny().orElse(null);
                    if (extensionShpDto != null) {

                        List<String> extensionsShp = new ArrayList<>(Arrays.asList("shp", "dbf", "shx", "prj"));

                        assert loadedFileExtension != null;
                        if (loadedFileExtension.equalsIgnoreCase("zip")) {

                            isLoadShp = ZipUtil.zipContainsFile(filePathTemporal, extensionsShp);
                            if (isLoadShp) {
                                fileAllowed = ZipUtil.zipMustContains(filePathTemporal, extensionsShp);
                                loadedFileExtensions = extensionsShp;
                                zipFile = false;
                            }

                        }

                    }

                    // verify if the supply is a gdb file
                    boolean isLoadGdb = false;
                    MicroserviceExtensionDto extensionGdbDto = extensionAllowed.stream()
                            .filter(ext -> ext.getName().equalsIgnoreCase("gdb")).findAny().orElse(null);
                    if (extensionGdbDto != null) {

                        if (loadedFileExtension.equalsIgnoreCase("zip")) {
                            isLoadGdb = ZipUtil.hasGDBDatabase(filePathTemporal);
                            if (isLoadGdb) {
                                fileAllowed = true;
                            }
                        }
                    }

                    assert loadedFileExtension != null;
                    if (loadedFileExtension.equalsIgnoreCase("zip")) {

                        if (!isLoadShp && !isLoadGdb) {
                            List<String> extensionsAllowed = extensionAllowed.stream()
                                    .map(MicroserviceExtensionDto::getName).collect(Collectors.toList());

                            fileAllowed = ZipUtil.zipContainsFile(filePathTemporal, extensionsAllowed);
                            loadedFileExtensions = ZipUtil.getExtensionsFromZip(filePathTemporal);
                            zipFile = false;
                        }

                    } else {

                        MicroserviceExtensionDto extensionDto = extensionAllowed.stream()
                                .filter(ext -> ext.getName().equalsIgnoreCase(loadedFileExtension)).findAny()
                                .orElse(null);
                        fileAllowed = extensionDto != null;
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
                    String urlDocumentaryRepository = fileBusiness.saveFileToSystem(fileUploaded, urlBase, zipFile);

                    if (!supplyExtension.isEmpty()) {
                        supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_VALIDATING;

                        // validate xtf with ili validator
                        iliBusiness.startValidation(requestId, observations, urlDocumentaryRepository,
                                supplyRequested.getId(), userCode, supplyRequested.getModelVersion(), false,
                                skipErrors);

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

        // Update request
        try {

            updateSupply.setDelivered(delivered);
            updateSupply.setDeliveryBy(userCode);
            updateSupply.setSupplyRequestedStateId(supplyRequestedStateId);
            updateSupply.setJustification(justification);
            updateSupply.setExtraFile(urlExtraFileSaved);

            MicroserviceRequestDto responseRequestDto = providerClient.updateSupplyRequested(requestId,
                    supplyRequested.getId(), updateSupply);
            requestUpdatedDto = new CustomRequestDto(responseRequestDto);

            List<? extends MicroserviceSupplyRequestedDto> suppliesResponseDto = requestUpdatedDto
                    .getSuppliesRequested();
            List<CustomSupplyRequestedDto> suppliesRequestedDto = suppliesResponseDto.stream()
                    .map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

            for (CustomSupplyRequestedDto supply : suppliesRequestedDto) {
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

    public CustomRequestDto closeRequest(Long requestId, MicroserviceProviderDto providerDto, Long userCode)
            throws BusinessException {

        MicroserviceRequestDto response = providerClient.findRequestById(requestId);
        CustomRequestDto requestDto = new CustomRequestDto(response);

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

        List<? extends MicroserviceSupplyRequestedDto> suppliesResponse = requestDto.getSuppliesRequested();
        List<CustomSupplyRequestedDto> suppliesRequestDto = suppliesResponse.stream().map(CustomSupplyRequestedDto::new)
                .collect(Collectors.toList());

        boolean canClose = false;
        for (CustomSupplyRequestedDto supplyRequested : suppliesRequestDto) {
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

        CustomRequestDto requestUpdatedDto;

        try {

            List<? extends MicroserviceSupplyRequestedDto> suppliesResponseDto = requestDto.getSuppliesRequested();
            List<CustomSupplyRequestedDto> suppliesRequestedDto = suppliesResponseDto.stream()
                    .map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

            for (CustomSupplyRequestedDto supplyRequested : suppliesRequestedDto) {

                // verify if the supply is assigned to a task
                List<Long> taskStates = new ArrayList<>(
                        Arrays.asList(TaskBusiness.TASK_STATE_STARTED, TaskBusiness.TASK_STATE_ASSIGNED));
                List<Long> taskCategories = new ArrayList<>(
                        Collections.singletonList(TaskBusiness.TASK_CATEGORY_CADASTRAL_INPUT_GENERATION));

                List<MicroserviceTaskDto> responseTasksDto = taskClient.findByStateAndCategory(taskStates,
                        taskCategories);
                List<CustomTaskDto> tasksDto = responseTasksDto.stream().map(CustomTaskDto::new)
                        .collect(Collectors.toList());

                for (CustomTaskDto taskDto : tasksDto) {
                    MicroserviceTaskMetadataDto metadataRequest = taskDto.getMetadata().stream()
                            .filter(meta -> meta.getKey().equalsIgnoreCase("request")).findAny().orElse(null);
                    if (metadataRequest != null) {

                        MicroserviceTaskMetadataPropertyDto propertyRequest = metadataRequest.getProperties().stream()
                                .filter(p -> p.getKey().equalsIgnoreCase("requestId")).findAny().orElse(null);

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

            List<? extends MicroserviceSupplyRequestedDto> suppliesResponseDto = requestDto.getSuppliesRequested();
            List<CustomSupplyRequestedDto> suppliesRequestedDto = suppliesResponseDto.stream()
                    .map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

            for (CustomSupplyRequestedDto supplyRequested : suppliesRequestedDto) {

                if (supplyRequested.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED)) {

                    List<MicroserviceCreateSupplyAttachmentDto> attachments = new ArrayList<>();

                    if (supplyRequested.getTypeSupply().getId().equals(ProviderBusiness.PROVIDER_SUPPLY_CADASTRAL)) {
                        List<File> supplyFiles = new ArrayList<>(
                                Collections.singletonList(new File(supplyRequested.getUrl())));
                        if (supplyRequested.getLog() != null) {
                            supplyFiles.add(new File(supplyRequested.getLog()));
                        }
                        if (supplyRequested.getExtraFile() != null) {
                            supplyFiles.add(new File(supplyRequested.getExtraFile()));
                        }
                        String zipName = "insumo_" + RandomStringUtils.random(10, false, true);
                        String namespace = stFilesDirectory + "/" + requestDto.getMunicipalityCode().replace(" ", "_")
                                + "/insumos/proveedores/" + providerDto.getName().replace(" ", "_") + "/"
                                + supplyRequested.getTypeSupply().getName().replace(" ", "_");

                        String pathSupplyFile = ZipUtil.zipping(supplyFiles, zipName,
                                FileTool.removeAccents(namespace));

                        attachments.add(new MicroserviceCreateSupplyAttachmentDto(pathSupplyFile,
                                SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY));
                    } else {

                        if (supplyRequested.getUrl() != null) {
                            attachments.add(new MicroserviceCreateSupplyAttachmentDto(supplyRequested.getUrl(),
                                    SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY));
                        }
                        if (supplyRequested.getFtp() != null) {
                            attachments.add(new MicroserviceCreateSupplyAttachmentDto(supplyRequested.getFtp(),
                                    SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_FTP));
                        }

                    }

                    List<? extends MicroserviceEmitterDto> emittersResponse = requestDto.getEmitters();
                    List<CustomEmitterDto> emittersDto = emittersResponse.stream().map(CustomEmitterDto::new)
                            .collect(Collectors.toList());

                    CustomEmitterDto emitterDto = emittersDto.stream()
                            .filter(e -> e.getEmitterType().equalsIgnoreCase("ENTITY")).findAny().orElse(null);

                    supplyBusiness.createSupply(requestDto.getMunicipalityCode(), supplyRequested.getObservations(),
                            supplyRequested.getTypeSupply().getId(), emitterDto.getEmitterCode(), attachments,
                            requestId, userCode, providerDto.getId(), null, null, supplyRequested.getModelVersion(),
                            SupplyBusiness.SUPPLY_STATE_ACTIVE, supplyRequested.getTypeSupply().getName(),
                            supplyRequested.getValid());
                }

            }

        } catch (Exception e) {
            log.error("No se ha podido crear los insumos: " + e.getMessage());
            throw new BusinessException("No se ha podido disponer los insumos al municipio.");
        }

        try {
            MicroserviceRequestDto responseUpdateDto = providerClient.closeRequest(requestId, userCode);
            requestUpdatedDto = new CustomRequestDto(responseUpdateDto);
        } catch (Exception e) {
            throw new BusinessException("No se ha podido actualizar la información de la solicitud.");
        }

        return requestUpdatedDto;
    }

    @Deprecated
    public CustomRequestDto closeRequest(Long requestId, Long userCode) {
        try {
            MicroserviceRequestDto response = providerClient.closeRequest(requestId, userCode);
            return new CustomRequestDto(response);
        } catch (Exception e) {
            log.error("Error cerrando solicitud: " + e.getMessage());
            return null;
        }
    }

    public List<MicroserviceProviderUserDto> getUsersByProvider(Long providerId, List<Long> profiles)
            throws BusinessException {

        List<MicroserviceProviderUserDto> usersDto;

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

    public List<CustomRequestDto> getRequestsByEmittersManager(Long managerCode) throws BusinessException {
        try {
            List<MicroserviceRequestDto> response = providerClient.findRequestsByEmmiters(managerCode, "ENTITY");
            return response.stream().map(CustomRequestDto::new).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error consultando solicitudes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public MicroserviceRequestPaginatedDto getRequestsByManagerAndMunicipality(int page, Long managerCode,
            String municipalityCode) throws BusinessException {

        MicroserviceRequestPaginatedDto data;

        try {

            data = providerClient.getRequestsByManagerAndMunicipality(managerCode, municipalityCode, page);

            List<? extends MicroserviceRequestDto> response = data.getItems();
            List<CustomRequestDto> requests = response.stream().map(CustomRequestDto::new).collect(Collectors.toList());

            for (CustomRequestDto requestDto : requests) {
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

    public List<CustomRequestDto> getRequestsByPackage(String packageLabel) {

        List<CustomRequestDto> requestsDto = new ArrayList<>();

        try {

            List<MicroserviceRequestDto> response = providerClient.getRequestsByPackage(packageLabel);
            requestsDto = response.stream().map(CustomRequestDto::new).collect(Collectors.toList());

            for (CustomRequestDto requestDto : requestsDto) {
                requestDto = this.completeInformationRequest(requestDto);
            }

        } catch (Exception e) {
            log.error("Error consultando solicitudes por paquete: " + e.getMessage());
        }

        return requestsDto;
    }

    public MicroserviceRequestPaginatedDto getRequestsByManagerAndProvider(int page, Long managerCode, Long providerId)
            throws BusinessException {

        MicroserviceRequestPaginatedDto data;

        try {

            data = providerClient.getRequestsByManagerAndProvider(managerCode, providerId, page);

            List<? extends MicroserviceRequestDto> response = data.getItems();
            List<CustomRequestDto> requests = response.stream().map(CustomRequestDto::new).collect(Collectors.toList());

            List<CustomRequestDto> all = new ArrayList<>();

            for (CustomRequestDto requestDto : requests) {
                requestDto = this.completeInformationRequest(requestDto);
                all.add(requestDto);
            }

            data.setItems(all);

        } catch (BusinessException e) {
            log.error("Error consultando solicitudes por gestor y proveedor: " + e.getMessage());
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("Error consultando solicitudes por gestor y proveedor: " + e.getMessage());
            throw new BusinessException("No se ha podido consultar las solicitudes que el gestor ha realizado.");
        }

        return data;
    }

    public List<RequestPackageDto> getRequestsByManagerAndPackage(Long managerCode, String packageLabel)
            throws BusinessException {

        List<RequestPackageDto> packages = new ArrayList<>();
        List<CustomRequestDto> requests;

        List<String> labels = new ArrayList<>();

        try {

            List<MicroserviceRequestDto> response;

            if (packageLabel != null) {
                response = providerClient.getRequestsByManagerAndPackage(managerCode, packageLabel);
            } else {
                response = providerClient.findRequestsByEmmiters(managerCode, "ENTITY");
            }

            requests = response.stream().map(CustomRequestDto::new).collect(Collectors.toList());

            for (CustomRequestDto requestDto : requests) {

                requestDto = this.completeInformationRequest(requestDto);

                String packageRequest = requestDto.getPackageLabel();

                if (!labels.contains(packageRequest)) {
                    RequestPackageDto data = new RequestPackageDto();
                    data.setPackageLabel(packageRequest);
                    data.getRequests().add(requestDto);
                    packages.add(data);
                    labels.add(packageRequest);
                } else {

                    RequestPackageDto packageFound = packages.stream()
                            .filter(p -> p.getPackageLabel().equals(packageRequest)).findAny().orElse(null);
                    if (packageFound instanceof RequestPackageDto) {
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

    public CustomRequestDto completeInformationRequest(CustomRequestDto requestDto) {

        List<CustomEmitterDto> emittersDto = new ArrayList<>();

        List<? extends MicroserviceEmitterDto> emittersResponse = requestDto.getEmitters();
        List<CustomEmitterDto> emitterListDto = emittersResponse.stream().map(CustomEmitterDto::new)
                .collect(Collectors.toList());

        for (CustomEmitterDto emitterDto : emitterListDto) {
            if (emitterDto.getEmitterType().equals("ENTITY")) {
                try {
                    MicroserviceManagerDto managerDto = managerBusiness.getManagerById(emitterDto.getEmitterCode());
                    emitterDto.setUser(managerDto);
                } catch (Exception e) {
                    emitterDto.setUser(null);
                }
            } else {
                try {
                    MicroserviceUserDto userDto = administrationBusiness.getUserById(emitterDto.getEmitterCode());
                    emitterDto.setUser(userDto);
                } catch (Exception e) {
                    emitterDto.setUser(null);
                }
            }
            emittersDto.add(emitterDto);
        }

        MunicipalityEntity municipalityEntity = municipalityService
                .getMunicipalityByCode(requestDto.getMunicipalityCode());

        if (municipalityEntity != null) {
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

        List<? extends MicroserviceSupplyRequestedDto> suppliesResponse = requestDto.getSuppliesRequested();
        List<CustomSupplyRequestedDto> suppliesRequestDto = suppliesResponse.stream().map(CustomSupplyRequestedDto::new)
                .collect(Collectors.toList());

        for (CustomSupplyRequestedDto supply : suppliesRequestDto) {

            if (supply.getDeliveredBy() != null) {

                try {

                    MicroserviceUserDto userDto = administrationBusiness.getUserById(supply.getDeliveredBy());
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

        MicroserviceProviderProfileDto profileDto;

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

        List<MicroserviceProviderProfileDto> profilesDto;

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

        MicroserviceProviderProfileDto profileDto;

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

        MicroserviceTypeSupplyDto typeSupplyDto;

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

        List<MicroserviceTypeSupplyDto> typesSuppliesDto;

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

        MicroserviceTypeSupplyDto typeSupplyDto;

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

    public MicroserviceProviderDto getProviderByUserAdministrator(Long userCode) {
        MicroserviceProviderDto providerDto = null;
        try {
            providerDto = providerClient.findProviderByAdministrator(userCode);
        } catch (Exception e) {
            String message = "No se ha podido consultar el proveedor a partir de usuario administrador: " + e.getMessage();
            SCMTracing.sendError(message);
            log.error(message);
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

        return providerDtoByTechnical;
    }

    public boolean userProviderIsDirector(Long userCode) {

        boolean isDirector = false;

        try {

            List<MicroserviceProviderRoleDto> providerRoles = providerClient.findRolesByUser(userCode);

            MicroserviceProviderRoleDto roleDirector = providerRoles.stream()
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

        boolean isDelegate = false;

        try {

            List<MicroserviceProviderRoleDto> providerRoles = providerClient.findRolesByUser(userCode);

            MicroserviceProviderRoleDto roleDirector = providerRoles.stream()
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

    public CustomSupplyRequestedDto getSupplyRequestedById(Long supplyRequestedId) {
        CustomSupplyRequestedDto supplyRequestedDto = null;
        try {
            MicroserviceSupplyRequestedDto response = providerClient.getSupplyRequested(supplyRequestedId);
            supplyRequestedDto = new CustomSupplyRequestedDto(response);
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

    public CustomRequestDto updateStateToSupplyRequested(Long requestId, Long supplyRequestedId, Long stateId) {

        CustomRequestDto requestDto = null;

        try {

            MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();
            updateSupply.setSupplyRequestedStateId(stateId);

            MicroserviceRequestDto response = providerClient.updateSupplyRequested(requestId, supplyRequestedId,
                    updateSupply);

            requestDto = new CustomRequestDto(response);
        } catch (Exception e) {
            log.error("No se ha podido actualizar el estado del insumo solicitado: " + e.getMessage());
        }

        return requestDto;
    }

    public CustomRequestDto updateSupplyRequested(Long requestId, Long supplyRequestedId,
            MicroserviceUpdateSupplyRequestedDto updateSupplyData) {

        CustomRequestDto requestDto = null;

        try {
            MicroserviceRequestDto response = providerClient.updateSupplyRequested(requestId, supplyRequestedId,
                    updateSupplyData);
            requestDto = new CustomRequestDto(response);
        } catch (Exception e) {
            log.error("No se ha podido actualizar el insumo solicitado: " + e.getMessage());
        }

        return requestDto;
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

    public CustomRequestDto getRequestById(Long requestId) {

        CustomRequestDto requestDto = null;

        try {
            MicroserviceRequestDto response = providerClient.findRequestById(requestId);
            requestDto = new CustomRequestDto(response);
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

    public CustomPetitionDto createPetition(Long providerId, Long managerId, String description)
            throws BusinessException {

        CustomPetitionDto petitionDto;

        // validate provider
        MicroserviceProviderDto providerDto = null;
        try {
            providerDto = providerClient.findById(providerId);
        } catch (Exception e) {
            log.error("Error verificando proveedor para crear petición: " + e.getMessage());
        }
        if (providerDto == null) {
            throw new BusinessException("El proveedor de insumo no existe.");
        }

        try {

            MicroserviceCreatePetitionDto data = new MicroserviceCreatePetitionDto();
            data.setManagerCode(managerId);
            data.setObservations(description);

            MicroservicePetitionDto response = providerClient.createPetition(providerId, data);
            petitionDto = new CustomPetitionDto(response);

            petitionDto = addAdditionalDataToPetition(petitionDto);

        } catch (Exception e) {
            log.error("Error creando petición: " + e.getMessage());
            throw new BusinessException("No se ha podido crear la petición.");
        }

        return petitionDto;
    }

    public List<CustomPetitionDto> getPetitionsForManager(Long providerId, Long managerId) throws BusinessException {

        List<CustomPetitionDto> listPetitionsDto;
        List<MicroservicePetitionDto> response;

        if (providerId == null) {
            response = providerClient.getPetitionsByManager(managerId);
        } else {

            // validate provider
            MicroserviceProviderDto providerDto = null;
            try {
                providerDto = providerClient.findById(providerId);
            } catch (Exception e) {
                log.error("Error verificando proveedor para crear petición: " + e.getMessage());
            }
            if (providerDto == null) {
                throw new BusinessException("El proveedor de insumo no existe.");
            }

            response = providerClient.getPetitionsForManager(providerId, managerId);
        }

        listPetitionsDto = response.stream().map(CustomPetitionDto::new).collect(Collectors.toList());

        try {

            for (CustomPetitionDto petitionDto : listPetitionsDto) {
                petitionDto = addAdditionalDataToPetition(petitionDto);
            }

        } catch (Exception e) {
            log.error("Error obteniendo las peticiones para el gestor: " + e.getMessage());
            throw new BusinessException("No se ha podido consultar las peticiones del gestor.");
        }

        return listPetitionsDto;
    }

    public List<CustomPetitionDto> getPetitionsForProviderOpen(Long providerId) throws BusinessException {

        List<CustomPetitionDto> listPetitionsDto;

        // validate provider
        MicroserviceProviderDto providerDto = null;
        try {
            providerDto = providerClient.findById(providerId);
        } catch (Exception e) {
            log.error("Error verificando proveedor para crear petición: " + e.getMessage());
        }
        if (providerDto == null) {
            throw new BusinessException("El proveedor de insumo no existe.");
        }

        try {

            List<Long> states = new ArrayList<>(Collections.singletonList(ProviderBusiness.PETITION_STATE_PENDING));

            List<MicroservicePetitionDto> response = providerClient.getPetitionsForProvider(providerId, states);
            listPetitionsDto = response.stream().map(CustomPetitionDto::new).collect(Collectors.toList());

            for (CustomPetitionDto petitionDto : listPetitionsDto) {
                petitionDto = addAdditionalDataToPetition(petitionDto);
            }

        } catch (Exception e) {
            log.error("Error obteniendo las peticiones pendientes para el proveedor: " + e.getMessage());
            throw new BusinessException("No se ha podido consultar las peticiones del proveedor.");
        }

        return listPetitionsDto;
    }

    public List<CustomPetitionDto> getPetitionsForProviderClose(Long providerId) throws BusinessException {

        List<CustomPetitionDto> listPetitionsDto;

        // validate provider
        MicroserviceProviderDto providerDto = null;
        try {
            providerDto = providerClient.findById(providerId);
        } catch (Exception e) {
            log.error("Error verificando proveedor para crear petición: " + e.getMessage());
        }
        if (providerDto == null) {
            throw new BusinessException("El proveedor de insumo no existe.");
        }

        try {

            List<Long> states = new ArrayList<>(
                    Arrays.asList(ProviderBusiness.PETITION_STATE_ACCEPT, ProviderBusiness.PETITION_STATE_REJECT));

            List<MicroservicePetitionDto> response = providerClient.getPetitionsForProvider(providerId, states);
            listPetitionsDto = response.stream().map(CustomPetitionDto::new).collect(Collectors.toList());

            for (CustomPetitionDto petitionDto : listPetitionsDto) {
                petitionDto = addAdditionalDataToPetition(petitionDto);
            }

        } catch (Exception e) {
            log.error("Error obteniendo las peticiones cerradas para el proveedor: " + e.getMessage());
            throw new BusinessException("No se ha podido consultar las peticiones del proveedor.");
        }

        return listPetitionsDto;
    }

    public CustomPetitionDto acceptPetition(Long providerId, Long petitionId, String justification)
            throws BusinessException {

        CustomPetitionDto petitionDto;

        // validate provider
        MicroserviceProviderDto providerDto = null;
        try {
            providerDto = providerClient.findById(providerId);
        } catch (Exception e) {
            log.error("Error verificando proveedor para actualizar petición: " + e.getMessage());
        }
        if (providerDto == null) {
            throw new BusinessException("El proveedor de insumo no existe.");
        }

        try {

            MicroserviceUpdatePetitionDto data = new MicroserviceUpdatePetitionDto();
            data.setJustitication(justification);
            data.setPetitionStateId(ProviderBusiness.PETITION_STATE_ACCEPT);

            MicroservicePetitionDto response = providerClient.updatePetition(providerId, petitionId, data);
            petitionDto = new CustomPetitionDto(response);

            petitionDto = addAdditionalDataToPetition(petitionDto);

        } catch (BusinessException e) {
            log.error("Error aceptando la petición: " + e.getMessage());
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("Error aceptando la petición: " + e.getMessage());
            throw new BusinessException("No se ha podido aceptar la petición.");
        }

        return petitionDto;
    }

    public CustomPetitionDto rejectPetition(Long providerId, Long petitionId, String justification)
            throws BusinessException {

        CustomPetitionDto petitionDto;

        // validate provider
        MicroserviceProviderDto providerDto = null;
        try {
            providerDto = providerClient.findById(providerId);
        } catch (Exception e) {
            log.error("Error verificando proveedor para actualizar petición: " + e.getMessage());
        }
        if (providerDto == null) {
            throw new BusinessException("El proveedor de insumo no existe.");
        }

        try {

            MicroserviceUpdatePetitionDto data = new MicroserviceUpdatePetitionDto();
            data.setJustitication(justification);
            data.setPetitionStateId(ProviderBusiness.PETITION_STATE_REJECT);

            MicroservicePetitionDto response = providerClient.updatePetition(providerId, petitionId, data);
            petitionDto = new CustomPetitionDto(response);

            petitionDto = addAdditionalDataToPetition(petitionDto);

        } catch (BusinessException e) {
            log.error("Error rechazando la petición: " + e.getMessage());
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("Error rechazando la petición: " + e.getMessage());
            throw new BusinessException("No se ha podido rechazar la petición.");
        }

        return petitionDto;
    }

    private CustomPetitionDto addAdditionalDataToPetition(CustomPetitionDto petitionDto) {

        try {

            MicroserviceManagerDto managerDto = managerBusiness.getManagerById(petitionDto.getManagerCode());
            petitionDto.setManager(managerDto);

        } catch (Exception e) {
            petitionDto.setManager(null);
            log.error("Error agregando información adicional a una petición: " + e.getMessage());
        }

        return petitionDto;
    }

    public MicroserviceTypeSupplyDto enableTypeSupply(Long providerId, Long typeSupplyId) throws BusinessException {

        MicroserviceTypeSupplyDto typeSupplyDto;

        boolean belongToProvider;
        try {

            List<MicroserviceTypeSupplyDto> typesSuppliesList = providerClient.getTypesSuppliesByProvider(providerId);
            MicroserviceTypeSupplyDto foundTypeSupplyDto = typesSuppliesList.stream()
                    .filter(t -> t.getId().equals(typeSupplyId)).findAny().orElse(null);

            belongToProvider = (foundTypeSupplyDto != null);

        } catch (Exception e) {
            belongToProvider = false;
            log.error("Error verificando si el tipo de insumo pertenece al proveedor: " + e.getMessage());
        }
        if (!belongToProvider) {
            throw new BusinessException("El tipo de insumo no pertenece al proveedor.");
        }

        try {

            typeSupplyDto = providerClient.enableTypeSupply(typeSupplyId);

        } catch (BusinessException e) {
            log.error("Error activando tipo de insumo del proveedor: " + e.getMessage());
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("Error activando tipo de insumo del proveedor: " + e.getMessage());
            throw new BusinessException("No se ha podido activar el tipo de insumo.");
        }

        return typeSupplyDto;
    }

    public MicroserviceTypeSupplyDto disableTypeSupply(Long providerId, Long typeSupplyId) throws BusinessException {

        MicroserviceTypeSupplyDto typeSupplyDto;

        boolean belongToProvider;
        try {

            List<MicroserviceTypeSupplyDto> typesSuppliesList = providerClient.getTypesSuppliesByProvider(providerId);
            MicroserviceTypeSupplyDto foundTypeSupplyDto = typesSuppliesList.stream()
                    .filter(t -> t.getId().equals(typeSupplyId)).findAny().orElse(null);

            belongToProvider = (foundTypeSupplyDto != null);

        } catch (Exception e) {
            belongToProvider = false;
            log.error("Error verificando si el tipo de insumo pertenece al proveedor: " + e.getMessage());
        }
        if (!belongToProvider) {
            throw new BusinessException("El tipo de insumo no pertenece al proveedor.");
        }

        try {

            typeSupplyDto = providerClient.disableTypeSupply(typeSupplyId);

        } catch (BusinessException e) {
            log.error("Error desactivando tipo de insumo del proveedor: " + e.getMessage());
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("Error desactivando tipo de insumo del proveedor: " + e.getMessage());
            throw new BusinessException("No se ha podido desactivar el tipo de insumo.");
        }

        return typeSupplyDto;
    }

}
