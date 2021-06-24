package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.ManagerFeignClient;
import com.ai.st.microservice.common.clients.ProviderFeignClient;
import com.ai.st.microservice.common.clients.SupplyFeignClient;
import com.ai.st.microservice.common.clients.UserFeignClient;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceCreateDeliverySupplyDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceOperatorUserDto;
import com.ai.st.microservice.common.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.common.dto.providers.*;
import com.ai.st.microservice.common.dto.supplies.MicroserviceSupplyAttachmentDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.common.dto.tasks.MicroserviceCreateTaskMetadataDto;
import com.ai.st.microservice.common.dto.tasks.MicroserviceCreateTaskPropertyDto;
import com.ai.st.microservice.common.dto.tasks.MicroserviceCreateTaskStepDto;
import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.business.RoleBusiness;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.CreateSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.IntegrationDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityToAssignDto;
import com.ai.st.microservice.workspaces.dto.TypeSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.ValidationMunicipalitiesDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceManagerDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.CustomDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.CustomSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomEmitterDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.CustomSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.supplies.CustomSupplyDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceManagerEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.services.IIntegrationService;
import com.ai.st.microservice.workspaces.services.IIntegrationStateService;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IWorkspaceOperatorService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;
import com.ai.st.microservice.workspaces.utils.FileTool;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

@Component
public class WorkspaceBusiness {

    private final Logger log = LoggerFactory.getLogger(WorkspaceBusiness.class);

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

    private final ManagerFeignClient managerClient;
    private final ProviderFeignClient providerClient;
    private final UserFeignClient userClient;
    private final SupplyFeignClient supplyClient;
    private final IMunicipalityService municipalityService;
    private final IWorkspaceService workspaceService;
    private final IIntegrationService integrationService;
    private final IIntegrationStateService integrationStateService;
    private final IWorkspaceOperatorService workspaceOperatorService;
    private final DatabaseIntegrationBusiness databaseIntegrationBusiness;
    private final CrytpoBusiness cryptoBusiness;
    private final IntegrationBusiness integrationBusiness;
    private final TaskBusiness taskBusiness;
    private final IliBusiness iliBusiness;
    private final ProviderBusiness providerBusiness;
    private final FileBusiness fileBusiness;
    private final SupplyBusiness supplyBusiness;
    private final OperatorMicroserviceBusiness operatorBusiness;
    private final NotificationBusiness notificationBusiness;
    private final ManagerMicroserviceBusiness managerBusiness;
    private final WorkspaceManagerBusiness workspaceManagerBusiness;
    private final WorkspaceOperatorBusiness workspaceOperatorBusiness;
    private final AdministrationBusiness administrationBusiness;

    public WorkspaceBusiness(ManagerFeignClient managerClient, ProviderFeignClient providerClient,
                             UserFeignClient userClient, SupplyFeignClient supplyClient, IMunicipalityService municipalityService,
                             IWorkspaceService workspaceService, IIntegrationService integrationService, IIntegrationStateService integrationStateService,
                             IWorkspaceOperatorService workspaceOperatorService, DatabaseIntegrationBusiness databaseIntegrationBusiness,
                             CrytpoBusiness cryptoBusiness, IntegrationBusiness integrationBusiness, TaskBusiness taskBusiness,
                             IliBusiness iliBusiness, ProviderBusiness providerBusiness, FileBusiness fileBusiness,
                             SupplyBusiness supplyBusiness, OperatorMicroserviceBusiness operatorBusiness, NotificationBusiness notificationBusiness,
                             ManagerMicroserviceBusiness managerBusiness, WorkspaceManagerBusiness workspaceManagerBusiness,
                             WorkspaceOperatorBusiness workspaceOperatorBusiness, AdministrationBusiness administrationBusiness) {
        this.managerClient = managerClient;
        this.providerClient = providerClient;
        this.userClient = userClient;
        this.supplyClient = supplyClient;
        this.municipalityService = municipalityService;
        this.workspaceService = workspaceService;
        this.integrationService = integrationService;
        this.integrationStateService = integrationStateService;
        this.workspaceOperatorService = workspaceOperatorService;
        this.databaseIntegrationBusiness = databaseIntegrationBusiness;
        this.cryptoBusiness = cryptoBusiness;
        this.integrationBusiness = integrationBusiness;
        this.taskBusiness = taskBusiness;
        this.iliBusiness = iliBusiness;
        this.providerBusiness = providerBusiness;
        this.fileBusiness = fileBusiness;
        this.supplyBusiness = supplyBusiness;
        this.operatorBusiness = operatorBusiness;
        this.notificationBusiness = notificationBusiness;
        this.managerBusiness = managerBusiness;
        this.administrationBusiness = administrationBusiness;
        this.workspaceManagerBusiness = workspaceManagerBusiness;
        this.workspaceOperatorBusiness = workspaceOperatorBusiness;
    }

    public List<WorkspaceDto> getWorkspacesByMunicipality(Long municipalityId, Long managerCode)
            throws BusinessException {

        List<WorkspaceDto> listWorkspacesDto = new ArrayList<>();

        // validate if the municipality exists
        MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
        if (municipalityEntity == null) {
            throw new BusinessException("El municipio no existe.");
        }

        if (managerCode != null) {

            WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
            if (workspaceActive != null) {


                WorkspaceManagerEntity workspaceManagerEntity =
                        workspaceActive.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
                if (workspaceManagerEntity == null) {
                    throw new BusinessException("El gestor no tiene acceso al municipio.");
                }

            }

        }

        List<WorkspaceEntity> listWorkspacesEntity = workspaceService.getWorkspacesByMunicipality(municipalityEntity);

        for (WorkspaceEntity workspaceEntity : listWorkspacesEntity) {
            WorkspaceDto workspaceDto = entityParseToDto(workspaceEntity);
            listWorkspacesDto.add(workspaceDto);
        }

        return listWorkspacesDto;
    }

    public WorkspaceDto assignOperator(Long workspaceId, Date startDate, Date endDate, Long operatorCode,
                                       Long numberParcelsExpected, Double workArea, MultipartFile supportFile, String observations,
                                       Long managerCode) throws BusinessException {


        // validate if the workspace exists
        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException(
                    "No se puede asignar el operador porque el espacio de trabajo no es el actual.");
        }

        // validate if the end date is greater than the start date
        if (!endDate.after(startDate)) {
            throw new BusinessException("La fecha de finalización debe ser mayor a la fecha de inicio.");
        }

        WorkspaceManagerEntity workspaceManagerEntity = workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
        if (workspaceManagerEntity == null) {
            throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
        }

        // validate if the operator exists
        MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorById(operatorCode);
        if (operatorDto == null) {
            throw new BusinessException("No se ha encontrado el operador.");
        }

        String extension = FilenameUtils.getExtension(supportFile.getOriginalFilename());
        if (!extension.equalsIgnoreCase("pdf")) {
            throw new BusinessException("El formato del soporte es inválido, se deben cargar archivos en formato pdf.");
        }

        String urlDocumentaryRepository;
        try {
            String urlBase = "/" + workspaceEntity.getMunicipality().getCode() + "/soportes/operadores";
            urlBase = FileTool.removeAccents(urlBase);
            urlDocumentaryRepository = fileBusiness.saveFileToSystem(supportFile, urlBase, false);
        } catch (Exception e) {
            log.error("No se ha podido cargar el soporte operador: " + e.getMessage());
            throw new BusinessException("No se ha podido cargar el soporte.");
        }

        WorkspaceOperatorEntity workspaceOperatorEntityFound = workspaceEntity.getOperators().stream()
                .filter(o -> o.getOperatorCode().equals(operatorCode) && o.getManagerCode().equals(managerCode))
                .findAny().orElse(null);

        if (workspaceOperatorEntityFound != null) {
            throw new BusinessException("El operador ya ha sido asignado al municipio.");
        }

        WorkspaceOperatorDto workspaceOperatorDto = workspaceOperatorBusiness.createOperator(startDate, endDate, numberParcelsExpected, workArea, observations,
                urlDocumentaryRepository, workspaceId, operatorCode, managerCode);

        // send notification
        try {

            MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();
            MicroserviceManagerDto managerDto = managerBusiness.getManagerById(managerCode);

            List<MicroserviceOperatorUserDto> operatorUsers = operatorBusiness.getUsersByOperator(operatorDto.getId());
            for (MicroserviceOperatorUserDto operatorUser : operatorUsers) {
                MicroserviceUserDto userDto = administrationBusiness.getUserById(operatorUser.getUserCode());
                if (userDto != null && userDto.getEnabled()) {
                    notificationBusiness.sendNotificationAssignmentOperation(userDto.getEmail(), userDto.getId(),
                            managerDto.getName(), municipalityEntity.getName(),
                            municipalityEntity.getDepartment().getName(), startDate, endDate, "");
                }
            }
        } catch (Exception e) {
            log.error("Error enviando notificación al asignar operador: " + e.getMessage());
        }

        WorkspaceDto workspaceDto = entityParseToDto(workspaceEntity);
        workspaceDto.getOperators().add(workspaceOperatorDto);

        return workspaceDto;
    }

    public WorkspaceDto updateManagerFromWorkspace(Long workspaceId, Long managerCode, Date startDate,
                                                   String observations) throws BusinessException {

        WorkspaceDto workspaceDto;

        // validate if the workspace exists
        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo del municipio.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException("El municipio no tiene un espacio de trabajo activo.");
        }

        WorkspaceManagerEntity workspaceManagerFound = workspaceEntity.getManagers().stream()
                .filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
        if (workspaceManagerFound == null) {
            throw new BusinessException("El gestor no pertenece al municipio.");
        }

        workspaceManagerBusiness.updateWorkspaceManager(workspaceManagerFound.getId(), observations, startDate);

        workspaceEntity.setUpdatedAt(new Date());
        workspaceEntity = workspaceService.updateWorkspace(workspaceEntity);

        workspaceDto = entityParseToDto(workspaceEntity);
        return workspaceDto;
    }

    public WorkspaceDto getWorkspaceById(Long workspaceId, Long managerCode) throws BusinessException {

        // validate if the workspace exists
        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate access
        if (managerCode != null) {
            WorkspaceManagerEntity managerFound = workspaceEntity.getManagers().stream()
                    .filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
            if (managerFound == null) {
                throw new BusinessException("El usuario no tiene acceso al municipio.");
            }
        }

        return entityParseToDto(workspaceEntity);
    }

    public List<WorkspaceOperatorDto> getOperatorsByWorkspaceId(Long workspaceId, Long managerCode)
            throws BusinessException {

        List<WorkspaceOperatorDto> listOperatorsDto;

        // validate if the workspace exists
        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate access
        if (managerCode != null) {

            WorkspaceManagerEntity workspaceManagerEntity = workspaceEntity.getManagers()
                    .stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
            if (workspaceManagerEntity == null) {
                throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
            }

            listOperatorsDto = workspaceEntity.getOperators().stream().filter(o -> o.getManagerCode().equals(managerCode))
                    .map(workspaceOperatorBusiness::entityParseToDto).collect(Collectors.toList());

        } else {

            listOperatorsDto = workspaceEntity.getOperators().stream()
                    .map(workspaceOperatorBusiness::entityParseToDto).collect(Collectors.toList());

        }

        return listOperatorsDto;
    }

    public WorkspaceDto getWorkspaceActiveByMunicipality(Long municipalityId, Long managerCode)
            throws BusinessException {

        // validate if the municipality exists
        MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
        if (municipalityEntity == null) {
            throw new BusinessException("El municipio no existe.");
        }

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);

        if (workspaceEntity == null) {
            throw new BusinessException("No existe un espacio de trabajo para el municipio.");
        }

        // validate access
        if (managerCode != null) {

            WorkspaceManagerEntity workspaceManagerEntity =
                    workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
            if (workspaceManagerEntity == null) {
                throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
            }

        }

        return entityParseToDto(workspaceEntity);
    }

    public List<CustomRequestDto> createRequest(Date deadline, List<TypeSupplyRequestedDto> supplies,
                                                Long userCode, Long managerCode, Long municipalityId) throws BusinessException {

        // validate if the municipality exists
        MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
        if (municipalityEntity == null) {
            throw new BusinessException("El municipio no existe.");
        }

        // validate access
        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);


        if (workspaceEntity != null) {

            WorkspaceManagerEntity workspaceManagerEntity =
                    workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
            if (workspaceManagerEntity == null) {
                throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
            }

        } else {
            throw new BusinessException("El municipio no tiene un espacio de trabajo activo.");
        }

        // verify that the sea deadline greater than the 15 days
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 15);
            Date date15Days = sdf.parse(sdf.format(cal.getTime()));
            if (!deadline.after(date15Days)) {
                throw new BusinessException(
                        "La fecha límite debe tener 15 días de diferencia respecto a la fecha actual.");
            }
        } catch (Exception e) {
            throw new BusinessException("La fecha límite es inválida.");
        }

        List<MicroserviceCreateRequestDto> groupRequests = new ArrayList<>();
        List<Long> skipped = new ArrayList<>();

        String packageLabel;
        int count;

        do {
            packageLabel = RandomStringUtils.random(6, false, true).toLowerCase();
            count = providerBusiness.getRequestsByPackage(packageLabel).size();
        } while (count > 0);

        for (TypeSupplyRequestedDto supplyDto : supplies) {

            MicroserviceTypeSupplyDto typeSupplyDto = providerClient.findTypeSuppleById(supplyDto.getTypeSupplyId());
            Long profileId = typeSupplyDto.getProviderProfile().getId();

            if (!skipped.contains(profileId)) {

                MicroserviceCreateRequestDto requestDto = new MicroserviceCreateRequestDto();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                requestDto.setDeadline(sdf.format(deadline));
                requestDto.setProviderId(supplyDto.getProviderId());
                requestDto.setMunicipalityCode(municipalityEntity.getCode());
                requestDto.setPackageLabel(packageLabel);

                // supplies by request
                List<MicroserviceTypeSupplyRequestedDto> listSuppliesByProfile = new ArrayList<>();
                for (TypeSupplyRequestedDto supplyDto2 : supplies) {

                    MicroserviceTypeSupplyDto typeSupplyDto2 = providerClient
                            .findTypeSuppleById(supplyDto2.getTypeSupplyId());

                    if (typeSupplyDto2.getProviderProfile().getId().equals(profileId)) {

                        if (typeSupplyDto2.getModelRequired()
                                && (supplyDto2.getModelVersion() == null || supplyDto2.getModelVersion().isEmpty())) {
                            throw new BusinessException(
                                    "El tipo de insumo solicita que se especifique una versión del modelo.");
                        }

                        MicroserviceTypeSupplyRequestedDto typeSupplyRequestedDto = new MicroserviceTypeSupplyRequestedDto();
                        typeSupplyRequestedDto.setObservation(supplyDto2.getObservation());
                        typeSupplyRequestedDto.setTypeSupplyId(supplyDto2.getTypeSupplyId());
                        typeSupplyRequestedDto.setModelVersion(supplyDto2.getModelVersion());
                        listSuppliesByProfile.add(typeSupplyRequestedDto);
                    }
                }

                // emitters by request
                List<MicroserviceRequestEmitterDto> listEmittersByProvider = new ArrayList<>();
                MicroserviceRequestEmitterDto emitter1 = new MicroserviceRequestEmitterDto();
                emitter1.setEmitterCode(userCode);
                emitter1.setEmitterType("USER");
                listEmittersByProvider.add(emitter1);
                MicroserviceRequestEmitterDto emitter2 = new MicroserviceRequestEmitterDto();
                emitter2.setEmitterCode(managerCode);
                emitter2.setEmitterType("ENTITY");
                listEmittersByProvider.add(emitter2);

                requestDto.setSupplies(listSuppliesByProfile);
                requestDto.setEmitters(listEmittersByProvider);

                groupRequests.add(requestDto);
                skipped.add(profileId);
            }

        }

        MicroserviceManagerDto managerDto = managerBusiness.getManagerById(managerCode);

        List<CustomRequestDto> requests = new ArrayList<>();
        for (MicroserviceCreateRequestDto request : groupRequests) {

            try {

                MicroserviceRequestDto response = providerClient.createRequest(request);
                CustomRequestDto responseRequest = new CustomRequestDto(response);
                requests.add(responseRequest);

                // send notification
                try {

                    List<MicroserviceProviderUserDto> providerUsers = providerBusiness
                            .getUsersByProvider(request.getProviderId(), null);

                    Long providerProfileId = responseRequest.getSuppliesRequested().get(0).getTypeSupply()
                            .getProviderProfile().getId();

                    for (MicroserviceProviderUserDto providerUser : providerUsers) {

                        MicroserviceUserDto userDto = administrationBusiness.getUserById(providerUser.getUserCode());
                        if (userDto.getEnabled()) {
                            providerUser.getProfiles().stream()
                                    .filter(p -> p.getId().equals(providerProfileId)).findAny()
                                    .ifPresent(profileFound -> notificationBusiness.sendNotificationInputRequest(userDto.getEmail(), userCode,
                                            managerDto.getName(), municipalityEntity.getName(),
                                            municipalityEntity.getDepartment().getName(), responseRequest.getId().toString(),
                                            new Date()));
                        }


                    }

                } catch (Exception er) {
                    log.error("Error enviando la notificación por solicitud de insumos: " + er.getMessage());
                }

                if (responseRequest.getProvider().getId().equals(ProviderBusiness.PROVIDER_IGAC_ID)) {

                    List<? extends MicroserviceSupplyRequestedDto> suppliesResponse = responseRequest.getSuppliesRequested();
                    List<CustomSupplyRequestedDto> suppliesRequestDto =
                            suppliesResponse.stream().map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

                    CustomSupplyRequestedDto supplyRequested = suppliesRequestDto.stream()
                            .filter(sR -> sR.getTypeSupply().getId().equals(ProviderBusiness.PROVIDER_SUPPLY_CADASTRAL))
                            .findAny().orElse(null);

                    if (supplyRequested instanceof CustomSupplyRequestedDto) {
                        // new task

                        List<Long> profiles = new ArrayList<>();
                        profiles.add(ProviderBusiness.PROVIDER_PROFILE_CADASTRAL);
                        List<MicroserviceProviderUserDto> providerUsersDto = providerBusiness
                                .getUsersByProvider(responseRequest.getProvider().getId(), profiles);

                        try {

                            List<Long> users = new ArrayList<>();
                            for (MicroserviceProviderUserDto providerUserDto : providerUsersDto) {
                                users.add(providerUserDto.getUserCode());
                            }

                            taskBusiness.createTaskForGenerationSupply(users,
                                    municipalityEntity.getName().toLowerCase(), responseRequest.getId(),
                                    supplyRequested.getTypeSupply().getId(), null, supplyRequested.getModelVersion());

                        } catch (Exception e) {
                            log.error("No se ha podido crear la tarea de generación de insumos: " + e.getMessage());
                        }

                    }

                }

            } catch (BusinessException e) {
                log.error("No se ha podido crear la solicitud: " + e.getMessage());
            }

        }

        return requests;
    }

    public List<CustomRequestDto> getPendingRequestByProvider(Long userCode, Long providerId)
            throws BusinessException {

        List<CustomRequestDto> listPendingRequestsDto = new ArrayList<>();

        try {

            List<MicroserviceProviderUserDto> usersByProvider = providerClient.findUsersByProviderId(providerId);
            MicroserviceProviderUserDto userProviderFound = usersByProvider.stream()
                    .filter(user -> userCode.equals(user.getUserCode())).findAny().orElse(null);
            if (userProviderFound == null) {
                throw new BusinessException("El usuario no esta registrado como usuario para el proveedor de insumo.");
            }

            List<MicroserviceRequestDto> response = providerClient.getRequestsByProvider(providerId, ProviderBusiness.REQUEST_STATE_REQUESTED);

            List<CustomRequestDto> responseRequestsDto = response.stream().map(CustomRequestDto::new).collect(Collectors.toList());

            for (CustomRequestDto requestDto : responseRequestsDto) {

                List<CustomEmitterDto> emittersDto = new ArrayList<>();

                List<? extends MicroserviceEmitterDto> emittersResponse = requestDto.getEmitters();
                List<CustomEmitterDto> emitterDtoList =
                        emittersResponse.stream().map(CustomEmitterDto::new).collect(Collectors.toList());

                for (CustomEmitterDto emitterDto : emitterDtoList) {

                    if (emitterDto.getEmitterType().equals("ENTITY")) {
                        try {
                            MicroserviceManagerDto managerDto = managerClient.findById(emitterDto.getEmitterCode());
                            emitterDto.setUser(managerDto);
                        } catch (Exception e) {
                            emitterDto.setUser(null);
                        }
                    } else {
                        try {
                            MicroserviceUserDto userDto = userClient.findById(emitterDto.getEmitterCode());
                            emitterDto.setUser(userDto);
                        } catch (Exception e) {
                            emitterDto.setUser(null);
                        }
                    }
                    emittersDto.add(emitterDto);
                }

                MunicipalityEntity municipalityEntity = municipalityService
                        .getMunicipalityByCode(requestDto.getMunicipalityCode());

                DepartmentEntity departmentEntity = municipalityEntity.getDepartment();

                MunicipalityDto municipalityDto = new MunicipalityDto();
                municipalityDto.setCode(municipalityEntity.getCode());
                municipalityDto.setId(municipalityEntity.getId());
                municipalityDto.setName(municipalityEntity.getName());
                municipalityDto.setDepartment(new DepartmentDto(departmentEntity.getId(), departmentEntity.getName(),
                        departmentEntity.getCode()));

                requestDto.setEmitters(emittersDto);
                requestDto.setMunicipality(municipalityDto);

                List<? extends MicroserviceSupplyRequestedDto> suppliesResponse = requestDto.getSuppliesRequested();
                List<CustomSupplyRequestedDto> suppliesRequestDto =
                        suppliesResponse.stream().map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

                for (CustomSupplyRequestedDto supply : suppliesRequestDto) {

                    if (supply.getDeliveredBy() != null) {

                        try {

                            MicroserviceUserDto userDto = userClient.findById(supply.getDeliveredBy());
                            supply.setUserDeliveryBy(userDto);
                        } catch (Exception e) {
                            supply.setUserDeliveryBy(null);
                        }

                    }

                }

                // verify profiles user
                List<CustomSupplyRequestedDto> suppliesRequested = new ArrayList<>();

                int countNot = 0;

                List<? extends MicroserviceSupplyRequestedDto> suppliesResponseDto = requestDto.getSuppliesRequested();
                List<CustomSupplyRequestedDto> suppliesRequestedDto =
                        suppliesResponseDto.stream().map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

                for (CustomSupplyRequestedDto supply : suppliesRequestedDto) {

                    MicroserviceProviderProfileDto profileSupply = supply.getTypeSupply().getProviderProfile();

                    MicroserviceProviderProfileDto profileUser = userProviderFound.getProfiles().stream()
                            .filter(profile -> profileSupply.getId().equals(profile.getId())).findAny().orElse(null);

                    if (profileUser != null) {

                        if (supply.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_PENDING_REVIEW)
                                || supply.getState().getId()
                                .equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_SETTING_REVIEW)
                                || supply.getState().getId().equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_IN_REVIEW)
                                || supply.getState().getId()
                                .equals(ProviderBusiness.SUPPLY_REQUESTED_STATE_CLOSING_REVIEW)) {

                            supply.setCanUpload(false);
                            countNot++;

                        } else {
                            supply.setCanUpload(true);
                        }

                    } else {
                        supply.setCanUpload(false);
                        countNot++;
                    }

                    suppliesRequested.add(supply);

                }

                requestDto.setSuppliesRequested(suppliesRequested);

                if (suppliesRequested.size() != countNot) {
                    listPendingRequestsDto.add(requestDto);
                }

            }

        } catch (Exception e) {
            log.error("No se han podido cargar las solicitudes pendientes del proveedor: " + e.getMessage());
        }

        return listPendingRequestsDto;
    }

    public List<CustomRequestDto> getClosedRequestByProvider(Long userCode, Long providerId)
            throws BusinessException {

        List<CustomRequestDto> listClosedRequestsDto = new ArrayList<>();

        try {

            List<MicroserviceRequestDto> response = providerClient.getRequestsByProviderClosed(providerId, userCode);
            List<CustomRequestDto> responseRequestsDto = response.stream().map(CustomRequestDto::new).collect(Collectors.toList());

            for (CustomRequestDto requestDto : responseRequestsDto) {

                List<CustomEmitterDto> emittersDto = new ArrayList<>();

                List<? extends MicroserviceEmitterDto> emittersResponse = requestDto.getEmitters();
                List<CustomEmitterDto> emittersRequestDto =
                        emittersResponse.stream().map(CustomEmitterDto::new).collect(Collectors.toList());

                for (CustomEmitterDto emitterDto : emittersRequestDto) {
                    if (emitterDto.getEmitterType().equals("ENTITY")) {
                        try {
                            MicroserviceManagerDto managerDto = managerClient.findById(emitterDto.getEmitterCode());
                            emitterDto.setUser(managerDto);
                        } catch (Exception e) {
                            emitterDto.setUser(null);
                        }
                    } else {
                        try {
                            MicroserviceUserDto userDto = userClient.findById(emitterDto.getEmitterCode());
                            emitterDto.setUser(userDto);
                        } catch (Exception e) {
                            emitterDto.setUser(null);
                        }
                    }
                    emittersDto.add(emitterDto);
                }

                MunicipalityEntity municipalityEntity = municipalityService
                        .getMunicipalityByCode(requestDto.getMunicipalityCode());

                DepartmentEntity departmentEntity = municipalityEntity.getDepartment();

                MunicipalityDto municipalityDto = new MunicipalityDto();
                municipalityDto.setCode(municipalityEntity.getCode());
                municipalityDto.setId(municipalityEntity.getId());
                municipalityDto.setName(municipalityEntity.getName());
                municipalityDto.setDepartment(new DepartmentDto(departmentEntity.getId(), departmentEntity.getName(),
                        departmentEntity.getCode()));

                requestDto.setEmitters(emittersDto);
                requestDto.setMunicipality(municipalityDto);

                try {
                    if (requestDto.getClosedBy() != null) {
                        MicroserviceUserDto userDto = userClient.findById(requestDto.getClosedBy());
                        requestDto.setUserClosedBy(userDto);
                    }
                } catch (Exception e) {
                    requestDto.setUserClosedBy(null);
                }

                List<? extends MicroserviceSupplyRequestedDto> suppliesResponse = requestDto.getSuppliesRequested();
                List<CustomSupplyRequestedDto> suppliesRequestDto =
                        suppliesResponse.stream().map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

                for (CustomSupplyRequestedDto supply : suppliesRequestDto) {

                    if (supply.getDeliveredBy() != null) {

                        try {

                            MicroserviceUserDto userDto = userClient.findById(supply.getDeliveredBy());
                            supply.setUserDeliveryBy(userDto);
                        } catch (Exception e) {
                            supply.setUserDeliveryBy(null);
                        }

                    }

                }

                listClosedRequestsDto.add(requestDto);

            }

        } catch (Exception e) {
            log.error("No se han podido cargar las solicitudes cerradas del proveedor: " + e.getMessage());
        }

        return listClosedRequestsDto;
    }

    public IntegrationDto makeIntegrationCadastreRegistration(Long municipalityId, Long supplyIdCadastre,
                                                              Long supplyIdRegistration,
                                                              MicroserviceManagerDto managerDto,
                                                              MicroserviceUserDto userDto)
            throws BusinessException {

        IntegrationDto integrationResponseDto;

        // validate if the municipality exists
        MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
        if (municipalityEntity == null) {
            throw new BusinessException("El municipio no existe.");
        }

        WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
        if (workspaceActive == null) {
            throw new BusinessException("El municipio no cuenta con un espacio de trabajo activo.");
        }

        WorkspaceManagerEntity workspaceManagerEntity =
                workspaceActive.getManagers().stream().filter(m -> m.getManagerCode().equals(managerDto.getId())).findAny().orElse(null);
        if (workspaceManagerEntity == null) {
            throw new BusinessException("No tiene acceso al municipio.");
        }

        // validate if there is an integration running
        List<Long> statesId = new ArrayList<>();
        statesId.add(IntegrationStateBusiness.STATE_STARTED_AUTOMATIC);
        statesId.add(IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC);
        statesId.add(IntegrationStateBusiness.STATE_STARTED_ASSISTED);
        statesId.add(IntegrationStateBusiness.STATE_FINISHED_ASSISTED);

        List<IntegrationEntity> integrationsPending = integrationService
                .getPendingIntegrations(workspaceActive.getId(), statesId, managerDto.getId());

        if (integrationsPending.size() > 0) {
            throw new BusinessException("Existe una integración en curso para el municipio.");
        }

        // validate cadastre
        CustomSupplyDto supplyCadastreDto;
        String pathFileCadastre;
        try {

            MicroserviceSupplyDto response = supplyClient.findSupplyById(supplyIdCadastre);
            supplyCadastreDto = new CustomSupplyDto(response);

            MicroserviceTypeSupplyDto typeSupplyDto = providerClient
                    .findTypeSuppleById(supplyCadastreDto.getTypeSupplyCode());

            supplyCadastreDto.setTypeSupply(typeSupplyDto);

            MicroserviceSupplyAttachmentDto attachment = supplyCadastreDto.getAttachments().stream()
                    .filter(a -> a.getAttachmentType().getId().equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY))
                    .findAny().orElse(null);
            pathFileCadastre = attachment.getData();

        } catch (Exception e) {
            throw new BusinessException("No se ha podido consultar el tipo de insumo.");
        }
        if (supplyCadastreDto.getTypeSupply().getProvider().getProviderCategory().getId() != 1) {
            throw new BusinessException("El insumo de catastro es inválido.");
        }
        if (!supplyCadastreDto.getMunicipalityCode().equals(municipalityEntity.getCode())) {
            throw new BusinessException("El insumo no pertenece al municipio.");
        }

        // validate register
        CustomSupplyDto supplyRegisteredDto;
        String pathFileRegistration;
        try {

            MicroserviceSupplyDto response = supplyClient.findSupplyById(supplyIdRegistration);
            supplyRegisteredDto = new CustomSupplyDto(response);

            MicroserviceTypeSupplyDto typeSupplyDto = providerClient
                    .findTypeSuppleById(supplyRegisteredDto.getTypeSupplyCode());

            supplyRegisteredDto.setTypeSupply(typeSupplyDto);

            MicroserviceSupplyAttachmentDto attachment = supplyRegisteredDto.getAttachments().stream()
                    .filter(a -> a.getAttachmentType().getId().equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY))
                    .findAny().orElse(null);
            pathFileRegistration = attachment.getData();

        } catch (Exception e) {
            throw new BusinessException("No se ha podido consultar el tipo de insumo.");
        }
        if (supplyRegisteredDto.getTypeSupply().getProvider().getProviderCategory().getId() != 2) {
            throw new BusinessException("El insumo de registro es inválido.");
        }
        if (!supplyRegisteredDto.getMunicipalityCode().equals(municipalityEntity.getCode())) {
            throw new BusinessException("El insumo no pertenece al municipio.");
        }

        if (pathFileCadastre == null || pathFileRegistration == null) {
            throw new BusinessException("No se puede realizar la integración con los insumos seleccionados.");
        }

        if (!supplyCadastreDto.getModelVersion().equals(supplyRegisteredDto.getModelVersion())) {
            throw new BusinessException(
                    "No se puede realizar la integración porque la versión del modelo de insumos es diferente.");
        }

        // validate if the integration has already been done
        IntegrationStateEntity stateGeneratedProduct = integrationStateService
                .getIntegrationStateById(IntegrationStateBusiness.STATE_GENERATED_PRODUCT);

        IntegrationEntity integrationDone = integrationService.getIntegrationByCadastreAndSnrAndState(
                supplyCadastreDto.getId(), supplyRegisteredDto.getId(), stateGeneratedProduct, managerDto.getId());
        if (integrationDone != null) {
            throw new BusinessException("Ya se ha hecho una integración con los insumos seleccionados.");
        }

        String randomDatabaseName = RandomStringUtils.random(8, true, false).toLowerCase();
        String randomUsername = RandomStringUtils.random(8, true, false).toLowerCase();
        String randomPassword = RandomStringUtils.random(10, true, true);

        // create database
        try {
            databaseIntegrationBusiness.createDatabase(randomDatabaseName, randomUsername, randomPassword);
        } catch (Exception e) {
            throw new BusinessException("No se ha podido iniciar la integración.");
        }

        Long integrationId;
        try {

            IntegrationStateEntity stateStarted = integrationStateService
                    .getIntegrationStateById(IntegrationStateBusiness.STATE_STARTED_AUTOMATIC);

            String textHistory = userDto.getFirstName() + " " + userDto.getLastName() + " - " + managerDto.getName();

            integrationResponseDto = integrationBusiness.createIntegration(
                    cryptoBusiness.encrypt(databaseIntegrationHost), cryptoBusiness.encrypt(databaseIntegrationPort),
                    cryptoBusiness.encrypt(randomDatabaseName), cryptoBusiness.encrypt(databaseIntegrationSchema),
                    cryptoBusiness.encrypt(randomUsername), cryptoBusiness.encrypt(randomPassword),
                    supplyCadastreDto.getId(), supplyRegisteredDto.getId(), null, workspaceActive, stateStarted,
                    userDto.getId(), managerDto.getId(), textHistory);

            integrationId = integrationResponseDto.getId();

        } catch (Exception e) {
            log.error("No se ha podido crear la integración: " + e.getMessage());
            throw new BusinessException("No se ha podido crear la integración.");
        }

        try {

            iliBusiness.startIntegration(pathFileCadastre, pathFileRegistration, databaseIntegrationHost,
                    randomDatabaseName, databaseIntegrationPassword, databaseIntegrationPort, databaseIntegrationSchema,
                    databaseIntegrationUsername, integrationId, supplyCadastreDto.getModelVersion().trim());

        } catch (Exception e) {
            integrationBusiness.updateStateToIntegration(integrationId,
                    IntegrationStateBusiness.STATE_ERROR_INTEGRATION_AUTOMATIC, e.getMessage(), null, null, "SISTEMA");
            log.error("No se ha podido iniciar la integración: " + e.getMessage());
            throw new BusinessException("No se ha podido iniciar la integración.");
        }

        return integrationResponseDto;
    }

    public IntegrationDto startIntegrationAssisted(Long workspaceId, Long integrationId,
                                                   MicroserviceManagerDto managerDto, MicroserviceUserDto userDto) throws BusinessException {

        IntegrationDto integrationDto;

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException(
                    "No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
        }

        WorkspaceManagerEntity workspaceManagerEntity =
                workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerDto.getId())).findAny().orElse(null);
        if (workspaceManagerEntity == null) {
            throw new BusinessException("No tiene acceso al municipio.");
        }

        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
        if (integrationEntity == null) {
            throw new BusinessException("No se ha encontrado la integración.");
        }

        if (!integrationEntity.getWorkspace().getId().equals(workspaceEntity.getId())) {
            throw new BusinessException("La integración no pertenece al espacio de trabajo.");
        }

        IntegrationStateEntity stateIntegrationEntity = integrationEntity.getState();
        if (!stateIntegrationEntity.getId().equals(IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC)
                && !stateIntegrationEntity.getId().equals(IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT)) {
            throw new BusinessException(
                    "No se puede iniciar la integración asistida ya que no se encuentra en estado que no lo permite.");
        }

        // modify integration state

        String textHistory = userDto.getFirstName() + " " + userDto.getLastName() + " - " + managerDto.getName();
        integrationDto = integrationBusiness.updateStateToIntegration(integrationId,
                IntegrationStateBusiness.STATE_STARTED_ASSISTED, null, userDto.getId(), managerDto.getId(), textHistory);

        String host = integrationEntity.getHostname();
        String port = integrationEntity.getPort();
        String database = integrationEntity.getDatabase();
        String schema = integrationEntity.getSchema();
        String username = integrationEntity.getUsername();
        String password = integrationEntity.getPassword();

        try {
            databaseIntegrationBusiness.protectedDatabase(cryptoBusiness.decrypt(host), cryptoBusiness.decrypt(port),
                    cryptoBusiness.decrypt(database), cryptoBusiness.decrypt(schema), cryptoBusiness.decrypt(username),
                    cryptoBusiness.decrypt(password));
        } catch (Exception e) {
            log.error("No se ha podido restringir la base de datos: " + e.getMessage());
        }

        // create task
        try {

            List<Long> profiles = new ArrayList<>();
            profiles.add(RoleBusiness.SUB_ROLE_INTEGRATOR);

            List<MicroserviceManagerUserDto> listUsersIntegrators = managerClient.findUsersByManager(managerDto.getId(),
                    profiles);

            List<Long> users = new ArrayList<>();
            for (MicroserviceManagerUserDto managerUserDto : listUsersIntegrators) {
                users.add(managerUserDto.getUserCode());
            }

            List<Long> taskCategories = new ArrayList<>();
            taskCategories.add(TaskBusiness.TASK_CATEGORY_INTEGRATION);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 30);

            MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

            String description = "Integración modelo de insumos catastro-registro "
                    + municipalityEntity.getName().toLowerCase();
            String name = "Integración catastro-registro " + municipalityEntity.getName().toLowerCase();

            List<MicroserviceCreateTaskMetadataDto> metadata = new ArrayList<>();

            MicroserviceCreateTaskMetadataDto metadataConnection = new MicroserviceCreateTaskMetadataDto();
            metadataConnection.setKey("connection");
            List<MicroserviceCreateTaskPropertyDto> listPropertiesConnection = new ArrayList<>();
            listPropertiesConnection.add(new MicroserviceCreateTaskPropertyDto("host", host));
            listPropertiesConnection.add(new MicroserviceCreateTaskPropertyDto("port", port));
            listPropertiesConnection.add(new MicroserviceCreateTaskPropertyDto("database", database));
            listPropertiesConnection.add(new MicroserviceCreateTaskPropertyDto("schema", schema));
            listPropertiesConnection.add(new MicroserviceCreateTaskPropertyDto("username", username));
            listPropertiesConnection.add(new MicroserviceCreateTaskPropertyDto("password", password));
            metadataConnection.setProperties(listPropertiesConnection);
            metadata.add(metadataConnection);

            MicroserviceCreateTaskMetadataDto metadataIntegration = new MicroserviceCreateTaskMetadataDto();
            metadataIntegration.setKey("integration");
            List<MicroserviceCreateTaskPropertyDto> listPropertiesIntegration = new ArrayList<>();
            listPropertiesIntegration
                    .add(new MicroserviceCreateTaskPropertyDto("integration", integrationEntity.getId().toString()));

            metadataIntegration.setProperties(listPropertiesIntegration);
            metadata.add(metadataIntegration);

            List<MicroserviceCreateTaskStepDto> steps = new ArrayList<>();

            taskBusiness.createTask(taskCategories, sdf.format(cal.getTime()), description, name, users, metadata,
                    steps);

            // send notification
            try {

                for (MicroserviceManagerUserDto managerUserDto : listUsersIntegrators) {

                    MicroserviceUserDto userIntegratorDto = administrationBusiness.getUserById(managerUserDto.getUserCode());
                    if (userIntegratorDto != null && userDto.getEnabled()) {
                        notificationBusiness.sendNotificationTaskAssignment(userIntegratorDto.getEmail(),
                                userIntegratorDto.getId(), name, municipalityEntity.getName(),
                                municipalityEntity.getDepartment().getName(), new Date());
                    }

                }

            } catch (Exception e) {
                log.error("Error enviando notificación a los usuarios que se les ha asignado una tarea de integración: "
                        + e.getMessage());
            }

        } catch (Exception e) {
            log.error("No se ha podido crear la tarea de integración: " + e.getMessage());
        }

        return integrationDto;
    }

    public IntegrationDto exportXtf(Long workspaceId, Long integrationId, MicroserviceManagerDto managerDto,
                                    MicroserviceUserDto userDto) throws BusinessException {

        IntegrationDto integrationDto;

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException(
                    "No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
        }

        WorkspaceManagerEntity workspaceManagerEntity =
                workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerDto.getId())).findAny().orElse(null);
        if (workspaceManagerEntity == null) {
            throw new BusinessException("No tiene acceso al municipio.");
        }

        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
        if (integrationEntity == null) {
            throw new BusinessException("No se ha encontrado la integración.");
        }

        if (!integrationEntity.getWorkspace().getId().equals(workspaceEntity.getId())) {
            throw new BusinessException("La integración no pertenece al espacio de trabajo.");
        }

        IntegrationStateEntity stateEntity = integrationEntity.getState();
        if (!stateEntity.getId().equals(IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC)
                && !stateEntity.getId().equals(IntegrationStateBusiness.STATE_FINISHED_ASSISTED)
                && !stateEntity.getId().equals(IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT)) {
            throw new BusinessException(
                    "No se puede generar el producto porque la integración se encuentra en un estado que no lo permite.");
        }

        try {

            String hostnameDecrypt = cryptoBusiness.decrypt(integrationEntity.getHostname());
            String databaseDecrypt = cryptoBusiness.decrypt(integrationEntity.getDatabase());
            String portDecrypt = cryptoBusiness.decrypt(integrationEntity.getPort());
            String schemaDecrypt = cryptoBusiness.decrypt(integrationEntity.getSchema());

            // supply cadastre
            MicroserviceSupplyDto response = supplyClient.findSupplyById(integrationEntity.getSupplyCadastreId());
            CustomSupplyDto supplyCadastreDto = new CustomSupplyDto(response);

            String urlBase = "/" + workspaceEntity.getMunicipality().getCode().replace(" ", "_")
                    + "/insumos/gestores/" + managerDto.getId();

            iliBusiness.startExport(hostnameDecrypt, databaseDecrypt, databaseIntegrationPassword, portDecrypt,
                    schemaDecrypt, databaseIntegrationUsername, integrationId, false,
                    supplyCadastreDto.getModelVersion(), urlBase);

            // modify integration state
            String textHistory = userDto.getFirstName() + " " + userDto.getLastName() + " - " + managerDto.getName();
            integrationDto = integrationBusiness.updateStateToIntegration(integrationId,
                    IntegrationStateBusiness.STATE_GENERATING_PRODUCT, null, userDto.getId(), managerDto.getId(),
                    textHistory);

        } catch (Exception e) {
            log.error("No se ha podido iniciar la generación del insumo: " + e.getMessage());
            throw new BusinessException("No se ha podido iniciar la generación del insumo");
        }

        return integrationDto;
    }

    public void removeIntegrationFromWorkspace(Long workspaceId, Long integrationId, Long managerCode)
            throws BusinessException {

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException(
                    "No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
        }

        IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
        if (integrationEntity == null) {
            throw new BusinessException("No se ha encontrado la integración.");
        }

        WorkspaceManagerEntity workspaceManagerEntity =
                workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)
                        && integrationEntity.getManagerCode().equals(m.getManagerCode())).findAny().orElse(null);
        if (workspaceManagerEntity == null) {
            throw new BusinessException("El gestor no tiene acceso al municipio.");
        }

        if (!integrationEntity.getWorkspace().getId().equals(workspaceEntity.getId())) {
            throw new BusinessException("La integración no pertenece al espacio de trabajo.");
        }

        IntegrationStateEntity stateEntity = integrationEntity.getState();
        if (!stateEntity.getId().equals(IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC)
                && !stateEntity.getId().equals(IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT)
                && !stateEntity.getId().equals(IntegrationStateBusiness.STATE_ERROR_INTEGRATION_AUTOMATIC)) {
            throw new BusinessException(
                    "No se puede generar el producto porque la integración se encuentra en un estado que no lo permite.");
        }

        try {

            integrationBusiness.deleteIntegration(integrationId);

        } catch (Exception e) {
            log.error("Error intentando eliminar la integración: " + e.getMessage());
            throw new BusinessException("No se ha podido eliminar la integración.");
        }

    }

    public boolean managerHasAccessToMunicipality(String municipalityCode, Long managerCode) {

        boolean hasAccess = false;

        MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityByCode(municipalityCode);
        if (municipalityEntity == null) {
            return false;
        }

        WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
        if (workspaceActive != null) {

            WorkspaceManagerEntity workspaceManagerEntity =
                    workspaceActive.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
            if (workspaceManagerEntity != null) {
                hasAccess = true;
            }
        }

        return hasAccess;
    }

    public void removeSupply(Long workspaceId, Long supplyId, Long managerCode) throws BusinessException {

        CustomSupplyDto supplyDto;
        List<String> pathsFile = new ArrayList<>();
        try {

            MicroserviceSupplyDto response = supplyClient.findSupplyById(supplyId);
            supplyDto = new CustomSupplyDto(response);

            for (MicroserviceSupplyAttachmentDto attachment : supplyDto.getAttachments()) {
                if (attachment.getAttachmentType().getId().equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY)
                        || attachment.getAttachmentType().getId()
                        .equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_EXTERNAL_SOURCE)) {
                    pathsFile.add(attachment.getData());
                }
            }

        } catch (Exception e) {
            log.error("No se ha encontrado el insumo para eliminarlo: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el insumo.");
        }

        if (managerCode != null) {

            WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
            if (workspaceEntity == null) {
                throw new BusinessException("No se ha encontrado el espacio de trabajo.");
            }

            // validate if the workspace is active
            if (!workspaceEntity.getIsActive()) {
                throw new BusinessException("El espacio de trabajo no se encuentra activo.");
            }

            WorkspaceManagerEntity workspaceManagerEntity =
                    workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
            if (workspaceManagerEntity == null) {
                throw new BusinessException("No tiene acceso al municipio.");
            }

            if (!supplyDto.getManagerCode().equals(managerCode)) {
                throw new BusinessException("No tiene acceso al insumo.");
            }

            MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

            if (!supplyDto.getMunicipalityCode().equals(municipalityEntity.getCode())) {
                throw new BusinessException("El insumo no pertenece al municipio.");
            }

        }

        supplyBusiness.deleteSupply(supplyId);

        for (String pathFile : pathsFile) {
            fileBusiness.deleteFile(pathFile);
        }

    }

    public CustomDeliveryDto createDelivery(Long workspaceId, Long managerCode, Long operatorCode, String observations,
                                            List<CreateSupplyDeliveryDto> suppliesDto) throws BusinessException {

        CustomDeliveryDto deliveryDto;

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException(
                    "No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
        }

        WorkspaceManagerEntity workspaceManagerEntity =
                workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
        if (workspaceManagerEntity == null) {
            throw new BusinessException("El gestor no tiene acceso al municipio.");
        }

        List<WorkspaceOperatorEntity> operators = workspaceEntity.getOperators();
        if (operators.size() == 0) {
            throw new BusinessException("El municipio no tiene asignado ningún operador.");
        }

        WorkspaceOperatorEntity workspaceOperatorEntity =
                operators.stream().filter(o -> o.getOperatorCode().equals(operatorCode) && o.getManagerCode().equals(managerCode)).
                        findAny().orElse(null);
        if (workspaceOperatorEntity == null) {
            throw new BusinessException("El municipio no tiene asignado el operador.");
        }

        MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

        List<MicroserviceCreateDeliverySupplyDto> microserviceSupplies = new ArrayList<>();

        // verify if the supplies exists
        List<CustomDeliveryDto> deliveriesDto = operatorBusiness.getDeliveriesByOperator(operatorCode,
                municipalityEntity.getCode());

        for (CreateSupplyDeliveryDto deliverySupplyDto : suppliesDto) {

            CustomSupplyDto supply = supplyBusiness.getSupplyById(deliverySupplyDto.getSupplyId());

            if (supply == null) {
                throw new BusinessException("No se ha encontrado el insumo.");
            }

            if (!supply.getManagerCode().equals(managerCode)) {
                throw new BusinessException("El gestor no tiene acceso al insumo.");
            }

            // verify if the supply has already delivered to operator
            for (CustomDeliveryDto deliveryFoundDto : deliveriesDto) {

                List<? extends MicroserviceSupplyDeliveryDto> suppliesResponse = deliveryFoundDto.getSupplies();
                List<CustomSupplyDeliveryDto> suppliesDeliveryDto =
                        suppliesResponse.stream().map(CustomSupplyDeliveryDto::new).collect(Collectors.toList());

                CustomSupplyDeliveryDto supplyFound = suppliesDeliveryDto.stream()
                        .filter(supplyDto -> supplyDto.getSupplyCode().equals(deliverySupplyDto.getSupplyId()))
                        .findAny().orElse(null);

                if (supplyFound != null) {

                    String nameSupply = (supply.getTypeSupply() != null) ? supply.getTypeSupply().getName() : supply.getObservations();

                    String messageError = String.format("El insumo %s ya ha sido entregado al operador.", nameSupply);
                    throw new BusinessException(messageError);
                }

            }

            microserviceSupplies.add(new MicroserviceCreateDeliverySupplyDto(deliverySupplyDto.getObservations(),
                    deliverySupplyDto.getSupplyId()));
        }

        try {
            deliveryDto = operatorBusiness.createDelivery(operatorCode, managerCode, municipalityEntity.getCode(),
                    observations, microserviceSupplies);

            try {

                MicroserviceManagerDto managerDto = managerBusiness.getManagerById(managerCode);

                List<MicroserviceOperatorUserDto> operatorUsers = operatorBusiness.getUsersByOperator(operatorCode);
                for (MicroserviceOperatorUserDto operatorUser : operatorUsers) {
                    MicroserviceUserDto userDto = administrationBusiness.getUserById(operatorUser.getUserCode());
                    if (userDto != null && userDto.getEnabled()) {
                        notificationBusiness.sendNotificationDeliverySupplies(userDto.getEmail(), userDto.getId(),
                                managerDto.getName(), municipalityEntity.getName(),
                                municipalityEntity.getDepartment().getName(), "", deliveryDto.getCreatedAt());
                    }
                }

            } catch (Exception e) {
                log.error("Error enviando notificación de entrega de insumos al operador: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("No se ha podido realizar la entrega al operador: " + e.getMessage());
            throw new BusinessException("No se ha podido realizar la entrega al operador.");
        }

        return deliveryDto;
    }

    public String getManagerSupportURL(Long workspaceId, Long managerCode, Long managerCodeSession) throws BusinessException {

        String supportURL = null;

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException(
                    "No se puede descargar el soporte, porque el municipio no tiene un espacio de trabajo activo.");
        }

        if (managerCodeSession != null) {
            WorkspaceManagerEntity workspaceManagerFound = workspaceEntity.getManagers().stream()
                    .filter(m -> m.getManagerCode().equals(managerCodeSession)).findAny().orElse(null);
            if (workspaceManagerFound == null) {
                throw new BusinessException("No tiene acceso al municipio.");
            }
        }

        WorkspaceManagerEntity workspaceManagerEntity = workspaceEntity.getManagers().stream()
                .filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
        if (workspaceManagerEntity != null) {
            supportURL = workspaceManagerEntity.getSupportFile();
        }

        return supportURL;
    }

    public List<WorkspaceDto> getWorkspacesByLocation(Long departmentId, Long municipalityId, Long managerCode)
            throws BusinessException {

        List<WorkspaceEntity> workspacesEntity = new ArrayList<>();

        if (municipalityId == null) {
            workspacesEntity = workspaceService.getWorkspacesByDepartment(departmentId);
        } else {

            MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
            if (municipalityEntity != null) {
                workspacesEntity = workspaceService.getWorkspacesByMunicipality(municipalityEntity);
            }

        }

        List<WorkspaceDto> workspacesDto = new ArrayList<>();

        for (WorkspaceEntity workspaceEntity : workspacesEntity) {

            if (managerCode != null) {

                workspaceEntity.getManagers().stream().filter(m -> m.getManagerCode().equals(managerCode)).findAny()
                        .ifPresent(workspaceManagerEntity -> workspacesDto.add(entityParseToDto(workspaceEntity)));

            } else {
                workspacesDto.add(entityParseToDto(workspaceEntity));
            }

        }

        return workspacesDto;
    }

    protected WorkspaceDto entityParseToDto(WorkspaceEntity workspaceEntity) {

        WorkspaceDto workspaceDto = new WorkspaceDto();
        workspaceDto.setId(workspaceEntity.getId());
        workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
        workspaceDto.setUpdatedAt(workspaceEntity.getUpdatedAt());
        workspaceDto.setIsActive(workspaceEntity.getIsActive());

        MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();
        DepartmentEntity departmentEntity = municipalityEntity.getDepartment();

        MunicipalityDto municipalityDto = new MunicipalityDto();
        municipalityDto.setId(municipalityEntity.getId());
        municipalityDto.setName(municipalityEntity.getName());
        municipalityDto.setCode(municipalityEntity.getCode());
        municipalityDto.setDepartment(
                new DepartmentDto(departmentEntity.getId(), departmentEntity.getName(), departmentEntity.getCode()));
        workspaceDto.setMunicipality(municipalityDto);

        List<WorkspaceManagerEntity> managers = workspaceEntity.getManagers();
        List<WorkspaceManagerDto> managersDto = new ArrayList<>();
        for (WorkspaceManagerEntity workspaceManagerEntity : managers) {
            WorkspaceManagerDto workspaceManagerDto = workspaceManagerBusiness.entityParseToDto(workspaceManagerEntity);
            managersDto.add(workspaceManagerDto);
        }
        workspaceDto.setManagers(managersDto);

        List<WorkspaceOperatorDto> operatorsDto = new ArrayList<>();
        for (WorkspaceOperatorEntity wOEntity : workspaceEntity.getOperators()) {
            WorkspaceOperatorDto workspaceOperatorDto = workspaceOperatorBusiness.entityParseToDto(wOEntity);
            operatorsDto.add(workspaceOperatorDto);
        }
        workspaceDto.setOperators(operatorsDto);

        return workspaceDto;
    }

    public void unassignedManagerFromMunicipality(Long municipalityId, Long managerCode) throws BusinessException {

        // check if the municipality exists
        MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
        if (municipalityEntity == null) {
            throw new BusinessException("No se ha encontrado el municipio");
        }

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
        if (workspaceEntity == null) {
            throw new BusinessException("El municipio no tiene asignado un gestor aún.");
        }

        WorkspaceManagerEntity workspaceManagerFound = workspaceEntity.getManagers().stream()
                .filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
        if (workspaceManagerFound == null) {
            throw new BusinessException("El gestor no tiene asignado el municipio.");
        }

        List<CustomRequestDto> requestsDto = providerBusiness.getRequestsByEmittersManager(managerCode);
        for (CustomRequestDto requestDto : requestsDto) {
            if (requestDto.getMunicipalityCode().equals(municipalityEntity.getCode())) {
                if (requestDto.getRequestState().getId().equals(ProviderBusiness.REQUEST_STATE_REQUESTED)) {
                    throw new BusinessException(
                            "No se puede desasignar el gestor del municipio porque ya existe un proceso de solicitud en curso.");
                }
            }
        }

        // delete integrations
        List<IntegrationDto> integrationsEntity = integrationBusiness
                .getIntegrationsByWorkspace(workspaceEntity.getId(), null).stream()
                .filter(i -> i.getManagerCode().equals(managerCode)).collect(Collectors.toList());
        for (IntegrationDto integrationEntity : integrationsEntity) {
            integrationBusiness.deleteIntegration(integrationEntity.getId());
        }

        // delete workspaces managers
        List<WorkspaceManagerEntity> managers = workspaceEntity.getManagers().stream()
                .filter(m -> m.getManagerCode().equals(managerCode)).collect(Collectors.toList());
        for (WorkspaceManagerEntity manager : managers) {
            workspaceManagerBusiness.deleteWorkspaceManagerById(manager.getId());
            fileBusiness.deleteFile(manager.getSupportFile());
        }

        // delete workspaces operators
        List<WorkspaceOperatorEntity> operators = workspaceEntity.getOperators().stream()
                .filter(o -> o.getManagerCode().equals(managerCode)).collect(Collectors.toList());
        for (WorkspaceOperatorEntity operator : operators) {
            workspaceOperatorBusiness.deleteWorkspaceOperatorById(operator.getId());
            fileBusiness.deleteFile(operator.getSupportFile());
        }

    }

    public List<ValidationMunicipalitiesDto> validateMunicipalitiesToAssign(List<Long> municipalities)
            throws BusinessException {

        List<ValidationMunicipalitiesDto> validations = new ArrayList<>();

        for (Long municipalityId : municipalities) {

            MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
            if (municipalityEntity != null) {

                WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);

                ValidationMunicipalitiesDto validation = new ValidationMunicipalitiesDto(municipalityId,
                        municipalityEntity.getName(), municipalityEntity.getCode());

                if (workspaceEntity != null) {

                    List<WorkspaceManagerEntity> workspaceManagers = workspaceEntity.getManagers();

                    int countManagers = workspaceManagers.size();

                    validation.setConflict(true);

                    if (countManagers > 0) {

                        for (WorkspaceManagerEntity workspaceManager : workspaceManagers) {
                            WorkspaceManagerDto workspaceManagerDto = new WorkspaceManagerDto();

                            MicroserviceManagerDto managerDto = managerBusiness
                                    .getManagerById(workspaceManager.getManagerCode());
                            if (managerDto != null) {
                                workspaceManagerDto.setManager(managerDto);
                            }

                            workspaceManagerDto.setManagerCode(workspaceManager.getManagerCode());
                            workspaceManagerDto.setObservations(workspaceManager.getObservations());
                            workspaceManagerDto.setStartDate(workspaceManager.getStartDate());
                            validation.getManagers().add(workspaceManagerDto);
                        }

                    }

                }

                validations.add(validation);
            }

        }

        return validations;
    }

    public List<WorkspaceDto> assignManager(Date startDate, Long managerCode,
                                            List<MunicipalityToAssignDto> municipalities, String observations, MultipartFile supportFile)
            throws BusinessException {

        List<WorkspaceDto> workspacesDto = new ArrayList<>();

        // validate if the manager exists
        MicroserviceManagerDto managerDto = managerBusiness.getManagerById(managerCode);
        if (managerDto == null) {
            throw new BusinessException("El gestor no existe");
        }

        String extension = FilenameUtils.getExtension(supportFile.getOriginalFilename());
        if (!extension.equalsIgnoreCase("pdf")) {
            throw new BusinessException("El formato del soporte es inválido, se deben cargar archivos en formato pdf.");
        }

        for (MunicipalityToAssignDto municipalityToAssign : municipalities) {

            // validate if the municipality exists
            MunicipalityEntity municipalityEntity = municipalityService
                    .getMunicipalityById(municipalityToAssign.getMunicipalityId());
            if (municipalityEntity == null) {
                throw new BusinessException("No se ha encontrado el municipio.");
            }

        }


        for (MunicipalityToAssignDto municipalityToAssign : municipalities) {

            MunicipalityEntity municipalityEntity = municipalityService
                    .getMunicipalityById(municipalityToAssign.getMunicipalityId());

            String urlDocumentaryRepository;
            try {

                String urlBase = "/" + municipalityEntity.getCode() + "/soportes/gestores";
                urlBase = FileTool.removeAccents(urlBase);
                urlDocumentaryRepository = fileBusiness.saveFileToSystem(supportFile, urlBase, false);

            } catch (Exception e) {
                log.error("No se ha podido cargar el soporte gestor: " + e.getMessage());
                throw new BusinessException("No se ha podido cargar el soporte.");
            }

            WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
            if (workspaceEntity == null) {

                workspaceEntity = new WorkspaceEntity();
                workspaceEntity.setCreatedAt(new Date());
                workspaceEntity.setIsActive(true);
                workspaceEntity.setMunicipality(municipalityEntity);
                workspaceEntity = workspaceService.createWorkspace(workspaceEntity);
            }

            List<WorkspaceManagerEntity> managers = workspaceEntity.getManagers();
            for (WorkspaceManagerEntity manager : managers) {
                if (manager.getManagerCode().equals(managerCode)) {
                    throw new BusinessException("El gestor ya se encuentra asignado al municipio.");
                }
            }

            WorkspaceManagerDto workspaceManagerDto = workspaceManagerBusiness.createWorkspaceManager(managerCode,
                    municipalityToAssign.getObservations(), observations, startDate, urlDocumentaryRepository,
                    workspaceEntity.getId());

            // send notification
            try {

                List<MicroserviceManagerUserDto> directors = managerBusiness.getUserByManager(managerDto.getId(),
                        new ArrayList<>(Collections.singletonList(RoleBusiness.SUB_ROLE_DIRECTOR)));

                for (MicroserviceManagerUserDto directorDto : directors) {

                    MicroserviceUserDto userDto = administrationBusiness.getUserById(directorDto.getUserCode());
                    if (userDto != null && userDto.getEnabled()) {
                        notificationBusiness.sendNotificationMunicipalityManagementDto(userDto.getEmail(), municipalityEntity.getDepartment().getName(),
                                municipalityEntity.getName(), startDate, userDto.getId(), "");
                    }

                }

            } catch (Exception e) {
                log.error("Error enviando notificación al asignar gestor: " + e.getMessage());
            }

            WorkspaceDto workspaceDto = entityParseToDto(workspaceEntity);
            workspaceDto.getManagers().add(workspaceManagerDto);
            workspacesDto.add(workspaceDto);

        }
        return workspacesDto;
    }

    public WorkspaceDto updateOperatorFromWorkspace(Long workspaceId, Long managerCode, Long operatorCode, Date startDate, Date endDate, String observations,
                                                    Long numberParcelsExpected, Double workArea, MultipartFile supportFile) throws BusinessException {

        WorkspaceDto workspaceDto;

        // validate if the workspace exists
        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo del municipio.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException("El municipio no tiene un espacio de trabajo activo.");
        }

        // validate if the end date is greater than the start date
        if (!endDate.after(startDate)) {
            throw new BusinessException("La fecha de finalización debe ser mayor a la fecha de inicio.");
        }

        WorkspaceManagerEntity workspaceManagerFound = workspaceEntity.getManagers().stream()
                .filter(m -> m.getManagerCode().equals(managerCode)).findAny().orElse(null);
        if (workspaceManagerFound == null) {
            throw new BusinessException("El gestor no pertenece al municipio.");
        }

        WorkspaceOperatorEntity workspaceOperatorFound = workspaceEntity.getOperators().stream().filter(o -> o.getOperatorCode().equals(operatorCode) && o.getManagerCode().equals(managerCode)).findAny().orElse(null);
        if (workspaceOperatorFound == null) {
            throw new BusinessException("El gestor no tiene permitido editar el operador.");
        }

        String urlDocumentaryRepository = null;
        if (!supportFile.isEmpty()) {

            try {
                String urlBase = "/" + workspaceEntity.getMunicipality().getCode() + "/soportes/operadores";
                urlBase = FileTool.removeAccents(urlBase);
                urlDocumentaryRepository = fileBusiness.saveFileToSystem(supportFile, urlBase, false);
            } catch (Exception e) {
                log.error("No se ha podido cargar el soporte operador: " + e.getMessage());
                throw new BusinessException("No se ha podido cargar el soporte.");
            }

        }

        workspaceOperatorBusiness.updateWorkspaceOperator(workspaceOperatorFound.getId(), startDate, endDate,
                observations, numberParcelsExpected, workArea, urlDocumentaryRepository);

        workspaceEntity.setUpdatedAt(new Date());
        workspaceEntity = workspaceService.updateWorkspace(workspaceEntity);

        workspaceDto = entityParseToDto(workspaceEntity);
        return workspaceDto;
    }

    public String getOperatorSupportURL(Long workspaceId, Long operatorCode, Long managerCodeSession) throws BusinessException {

        String supportURL;

        WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
        if (workspaceEntity == null) {
            throw new BusinessException("No se ha encontrado el espacio de trabajo.");
        }

        // validate if the workspace is active
        if (!workspaceEntity.getIsActive()) {
            throw new BusinessException(
                    "No se puede descargar el soporte, porque el municipio no tiene un espacio de trabajo activo.");
        }

        if (managerCodeSession != null) {
            WorkspaceManagerEntity workspaceManagerFound = workspaceEntity.getManagers().stream()
                    .filter(m -> m.getManagerCode().equals(managerCodeSession)).findAny().orElse(null);
            if (workspaceManagerFound == null) {
                throw new BusinessException("No tiene acceso al municipio.");
            }
        }

        WorkspaceOperatorEntity workspaceOperatorEntity = workspaceEntity.getOperators().stream()
                .filter(o -> o.getOperatorCode().equals(operatorCode)).findAny().orElse(null);
        if (workspaceOperatorEntity != null) {
            supportURL = workspaceOperatorEntity.getSupportFile();
        } else {
            throw new BusinessException("El operador no pertenece al municipio");
        }

        return supportURL;
    }

    public List<WorkspaceOperatorDto> getWorkspacesByOperator(Long operatorCode) {

        List<WorkspaceOperatorEntity> workspaceOperatorEntities =
                workspaceOperatorService.getWorkspacesOperatorsByOperatorCode(operatorCode);

        List<WorkspaceOperatorDto> workspacesOperators = new ArrayList<>();

        for (WorkspaceOperatorEntity operatorEntity : workspaceOperatorEntities) {
            WorkspaceOperatorDto workspaceOperatorDto = workspaceOperatorBusiness.entityParseToDto(operatorEntity);
            workspacesOperators.add(workspaceOperatorDto);
        }

        return workspacesOperators;
    }

}
