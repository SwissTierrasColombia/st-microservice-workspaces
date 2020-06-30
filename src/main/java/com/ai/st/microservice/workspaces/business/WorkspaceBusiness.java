package com.ai.st.microservice.workspaces.business;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.OperatorFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.CreateSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.IntegrationDto;
import com.ai.st.microservice.workspaces.dto.MilestoneDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.StateDto;
import com.ai.st.microservice.workspaces.dto.SupportDto;
import com.ai.st.microservice.workspaces.dto.TypeSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceOperatorDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateDeliverySupplyDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorUserDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceEmitterDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestEmitterDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyAttachmentDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskPropertyDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskStepDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MilestoneEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.StateEntity;
import com.ai.st.microservice.workspaces.entities.SupportEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceStateEntity;

import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IIntegrationService;
import com.ai.st.microservice.workspaces.services.IIntegrationStateService;
import com.ai.st.microservice.workspaces.services.IMilestoneService;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IStateService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;
import com.ai.st.microservice.workspaces.utils.FileTool;

import feign.FeignException;

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

	public static final Long WORKSPACE_CLONE_FROM_CHANGE_MANAGER = (long) 1;
	public static final Long WORKSPACE_CLONE_FROM_CHANGE_OPERATOR = (long) 2;

	@Autowired
	private ManagerFeignClient managerClient;

	@Autowired
	private OperatorFeignClient operatorClient;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private UserFeignClient userClient;

	@Autowired
	private SupplyFeignClient supplyClient;

	@Autowired
	private IMunicipalityService municipalityService;

	@Autowired
	private IWorkspaceService workspaceService;

	@Autowired
	private IMilestoneService milestoneService;

	@Autowired
	private IStateService stateService;

	@Autowired
	private IIntegrationService integrationService;

	@Autowired
	private IIntegrationStateService integrationStateService;

	@Autowired
	private DatabaseIntegrationBusiness databaseIntegrationBusiness;

	@Autowired
	private CrytpoBusiness cryptoBusiness;

	@Autowired
	private IntegrationBusiness integrationBusiness;

	@Autowired
	private TaskBusiness taskBusiness;

	@Autowired
	private IliBusiness iliBusiness;

	@Autowired
	private ProviderBusiness providerBusiness;

	@Autowired
	private FileBusiness fileBusiness;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private OperatorBusiness operatorBusiness;

	@Autowired
	private NotificationBusiness notificationBusiness;

	@Autowired
	private ManagerBusiness managerBusiness;

	@Autowired
	private UserBusiness userBusiness;

	public WorkspaceDto createWorkspace(Date startDate, Long managerCode, Long municipalityId, String observations,
			Long parcelsNumber, Double municipalityArea, MultipartFile supportFile) throws BusinessException {

		WorkspaceDto workspaceDto = null;

		// validate if the manager exists
		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(managerCode);
		} catch (Exception e) {
			throw new BusinessException("No se ha encontrado el gestor.");
		}

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("No se ha encontrado el municipio.");
		}

		// validate if workspace is active for municipality
		Long countWorkspaces = workspaceService.getCountByMunicipality(municipalityEntity);
		if (countWorkspaces > 0) {
			throw new BusinessException("Ya se ha creado un espacio de trabajo para el municipio.");
		}

		String urlDocumentaryRepository = null;
		try {

			String urlBase = "/" + municipalityEntity.getCode() + "/soportes/gestores";
			urlBase = FileTool.removeAccents(urlBase);
			urlDocumentaryRepository = fileBusiness.saveFileToSystem(supportFile, urlBase, true);

		} catch (Exception e) {
			log.error("No se ha podido cargar el soporte gestor: " + e.getMessage());
			throw new BusinessException("No se ha podido cargar el soporte.");
		}

		MilestoneEntity milestoneNewWorkspace = milestoneService
				.getMilestoneById(MilestoneBusiness.MILESTONE_NEW_WORKSPACE);

		StateEntity stateStart = stateService.getStateById(StateBusiness.STATE_START);

		WorkspaceEntity workspaceEntity = new WorkspaceEntity();
		workspaceEntity.setCreatedAt(new Date());
		workspaceEntity.setIsActive(true);
		workspaceEntity.setManagerCode(managerCode);
		workspaceEntity.setObservations(observations);
		workspaceEntity.setNumberAlphanumericParcels(parcelsNumber);
		workspaceEntity.setMunicipalityArea(municipalityArea);
		workspaceEntity.setStartDate(startDate);
		workspaceEntity.setVersion((long) 1);
		workspaceEntity.setMunicipality(municipalityEntity);
		workspaceEntity.setState(stateStart);

		// support
		SupportEntity supporEntity = new SupportEntity();
		supporEntity.setCreatedAt(new Date());
		supporEntity.setUrlDocumentaryRepository(urlDocumentaryRepository);
		supporEntity.setWorkspace(workspaceEntity);
		supporEntity.setMilestone(milestoneNewWorkspace);

		List<SupportEntity> supports = workspaceEntity.getSupports();
		supports.add(supporEntity);
		workspaceEntity.setSupports(supports);

		// states history
		WorkspaceStateEntity workspaceState = new WorkspaceStateEntity();
		workspaceState.setCreatedAt(new Date());
		workspaceState.setState(stateStart);
		workspaceState.setWorkspace(workspaceEntity);
		List<WorkspaceStateEntity> listStates = workspaceEntity.getStatesHistory();
		listStates.add(workspaceState);
		workspaceEntity.setStatesHistory(listStates);

		workspaceEntity = workspaceService.createWorkspace(workspaceEntity);

		// send notification
		try {

			List<MicroserviceManagerUserDto> directors = managerBusiness.getUserByManager(managerDto.getId(),
					new ArrayList<Long>(Arrays.asList(RoleBusiness.SUB_ROLE_DIRECTOR)));

			for (MicroserviceManagerUserDto directorDto : directors) {

				MicroserviceUserDto userDto = userBusiness.getUserById(directorDto.getUserCode());
				if (userDto instanceof MicroserviceUserDto) {
					notificationBusiness.sendNotificationMunicipalityManagementDto(userDto.getEmail(),
							municipalityEntity.getDepartment().getName(), municipalityEntity.getName(), startDate,
							userDto.getId(), "");
				}

			}

		} catch (Exception e) {
			log.error("Error enviando notificación al asignar gestor: " + e.getMessage());
		}

		workspaceDto = new WorkspaceDto();
		workspaceDto.setId(workspaceEntity.getId());
		workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
		workspaceDto.setUpdatedAt(workspaceEntity.getUdpatedAt());
		workspaceDto.setIsActive(workspaceEntity.getIsActive());
		workspaceDto.setManagerCode(workspaceEntity.getManagerCode());
		workspaceDto.setMunicipalityArea(workspaceEntity.getMunicipalityArea());
		workspaceDto.setNumberAlphanumericParcels(workspaceEntity.getNumberAlphanumericParcels());
		workspaceDto.setObservations(workspaceEntity.getObservations());
		workspaceDto.setStartDate(workspaceEntity.getStartDate());
		workspaceDto.setVersion(workspaceEntity.getVersion());
		workspaceDto.setManager(managerDto);
		workspaceDto.setState(new StateDto(workspaceEntity.getState().getId(), workspaceEntity.getState().getName(),
				workspaceEntity.getState().getDescription()));

		return workspaceDto;
	}

	public List<WorkspaceDto> getWorkspacesByMunicipality(Long municipalityId, Long codeManager)
			throws BusinessException {

		List<WorkspaceDto> listWorkspacesDto = new ArrayList<WorkspaceDto>();

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("El municipio no existe.");
		}

		if (codeManager != null) {

			WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
			if (workspaceActive instanceof WorkspaceEntity) {
				if (!codeManager.equals(workspaceActive.getManagerCode())) {
					throw new BusinessException("No tiene acceso al municipio.");
				}
			}

		}

		List<WorkspaceEntity> listWorkspacesEntity = workspaceService.getWorkspacesByMunicipality(municipalityEntity);

		for (WorkspaceEntity workspaceEntity : listWorkspacesEntity) {

			WorkspaceDto workspaceDto = new WorkspaceDto();
			workspaceDto.setId(workspaceEntity.getId());
			workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
			workspaceDto.setUpdatedAt(workspaceEntity.getUdpatedAt());
			workspaceDto.setIsActive(workspaceEntity.getIsActive());
			workspaceDto.setManagerCode(workspaceEntity.getManagerCode());
			workspaceDto.setMunicipalityArea(workspaceEntity.getMunicipalityArea());
			workspaceDto.setNumberAlphanumericParcels(workspaceEntity.getNumberAlphanumericParcels());
			workspaceDto.setObservations(workspaceEntity.getObservations());
			workspaceDto.setStartDate(workspaceEntity.getStartDate());
			workspaceDto.setVersion(workspaceEntity.getVersion());
			workspaceDto.setState(new StateDto(workspaceEntity.getState().getId(), workspaceEntity.getState().getName(),
					workspaceEntity.getState().getDescription()));

			// get the manager
			try {
				MicroserviceManagerDto managerDto = managerClient.findById(workspaceEntity.getManagerCode());
				workspaceDto.setManager(managerDto);
			} catch (Exception e) {
				workspaceDto.setManager(null);
			}

			List<WorkspaceOperatorDto> operatorsDto = new ArrayList<WorkspaceOperatorDto>();
			for (WorkspaceOperatorEntity wOEntity : workspaceEntity.getOperators()) {
				WorkspaceOperatorDto workspaceOperatorDto = new WorkspaceOperatorDto();
				workspaceOperatorDto.setId(wOEntity.getId());
				workspaceOperatorDto.setCreatedAt(wOEntity.getCreatedAt());
				workspaceOperatorDto.setEndDate(wOEntity.getEndDate());
				workspaceOperatorDto.setNumberParcelsExpected(wOEntity.getNumberParcelsExpected());
				workspaceOperatorDto.setOperatorCode(wOEntity.getOperatorCode());
				workspaceOperatorDto.setStartDate(wOEntity.getStartDate());
				workspaceOperatorDto.setWorkArea(wOEntity.getWorkArea());
				workspaceOperatorDto.setObservations(wOEntity.getObservations());

				// get operator
				try {
					MicroserviceOperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
					workspaceOperatorDto.setOperator(operatorDto);
				} catch (FeignException e) {
					workspaceOperatorDto.setOperator(null);
				}

				operatorsDto.add(workspaceOperatorDto);
			}

			workspaceDto.setOperators(operatorsDto);

			listWorkspacesDto.add(workspaceDto);
		}

		return listWorkspacesDto;
	}

	public WorkspaceDto assignOperator(Long workspaceId, Date startDate, Date endDate, Long operatorCode,
			Long numberParcelsExpected, Double workArea, MultipartFile supportFile, String observations,
			Long managerCode) throws BusinessException {

		WorkspaceDto workspaceDto = null;

		// validate if the workspace exists
		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
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

		// validate access
		if (!managerCode.equals(workspaceEntity.getManagerCode())) {
			throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
		}

		// validate if the operator exists
		MicroserviceOperatorDto operatorDto = null;
		try {
			operatorDto = operatorClient.findById(operatorCode);
		} catch (Exception e) {
			throw new BusinessException("No se ha encontrado el operador.");
		}

		// validate that the workspace does not already have an operator assigned
		if (workspaceEntity.getOperators().size() > 0) {

			// generate new workspace
			workspaceEntity = cloneWorkspace(workspaceId, WorkspaceBusiness.WORKSPACE_CLONE_FROM_CHANGE_OPERATOR);
		}

		String urlDocumentaryRepository = null;
		try {

			String urlBase = "/" + workspaceEntity.getMunicipality().getCode() + "/soportes/operadores";
			
			urlBase = FileTool.removeAccents(urlBase);
			
			urlDocumentaryRepository = fileBusiness.saveFileToSystem(supportFile, urlBase, true);

		} catch (Exception e) {
			log.error("No se ha podido cargar el soporte operador: " + e.getMessage());
			throw new BusinessException("No se ha podido cargar el soporte.");
		}

		MilestoneEntity milestoneAssignOperator = milestoneService
				.getMilestoneById(MilestoneBusiness.MILESTONE_OPERATOR_ASSIGNMENT);

		// operator
		WorkspaceOperatorEntity workspaceOperatorEntity = new WorkspaceOperatorEntity();
		workspaceOperatorEntity.setCreatedAt(new Date());
		workspaceOperatorEntity.setEndDate(endDate);
		workspaceOperatorEntity.setOperatorCode(operatorDto.getId());
		workspaceOperatorEntity.setStartDate(startDate);
		workspaceOperatorEntity.setNumberParcelsExpected(numberParcelsExpected);
		workspaceOperatorEntity.setObservations(observations);
		workspaceOperatorEntity.setWorkArea(workArea);
		workspaceOperatorEntity.setWorkspace(workspaceEntity);
		List<WorkspaceOperatorEntity> operators = workspaceEntity.getOperators();
		operators.add(workspaceOperatorEntity);

		// support
		SupportEntity supporEntity = new SupportEntity();
		supporEntity.setCreatedAt(new Date());
		supporEntity.setUrlDocumentaryRepository(urlDocumentaryRepository);
		supporEntity.setWorkspace(workspaceEntity);
		supporEntity.setMilestone(milestoneAssignOperator);
		List<SupportEntity> supports = workspaceEntity.getSupports();
		supports.add(supporEntity);

		workspaceEntity.setSupports(supports);
		workspaceEntity.setOperators(operators);

		workspaceEntity = workspaceService.updateWorkspace(workspaceEntity);

		// send notification
		try {

			MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

			MicroserviceManagerDto managerDto = managerBusiness.getManagerById(managerCode);

			List<MicroserviceOperatorUserDto> operatorUsers = operatorBusiness.getUsersByOperator(operatorDto.getId());
			for (MicroserviceOperatorUserDto operatorUser : operatorUsers) {
				MicroserviceUserDto userDto = userBusiness.getUserById(operatorUser.getUserCode());
				if (userDto instanceof MicroserviceUserDto) {
					notificationBusiness.sendNotificationAssignamentOperation(userDto.getEmail(), userDto.getId(),
							managerDto.getName(), municipalityEntity.getName(),
							municipalityEntity.getDepartment().getName(), startDate, endDate, "");
				}
			}

		} catch (Exception e) {
			log.error("Error enviando notificación al asignar operador: " + e.getMessage());
		}

		workspaceDto = new WorkspaceDto();
		workspaceDto.setId(workspaceEntity.getId());
		workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
		workspaceDto.setUpdatedAt(workspaceEntity.getUdpatedAt());
		workspaceDto.setIsActive(workspaceEntity.getIsActive());
		workspaceDto.setManagerCode(workspaceEntity.getManagerCode());
		workspaceDto.setMunicipalityArea(workspaceEntity.getMunicipalityArea());
		workspaceDto.setNumberAlphanumericParcels(workspaceEntity.getNumberAlphanumericParcels());
		workspaceDto.setObservations(workspaceEntity.getObservations());
		workspaceDto.setStartDate(workspaceEntity.getStartDate());
		workspaceDto.setVersion(workspaceEntity.getVersion());
		workspaceDto.setState(new StateDto(workspaceEntity.getState().getId(), workspaceEntity.getState().getName(),
				workspaceEntity.getState().getDescription()));

		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(workspaceEntity.getManagerCode());
			workspaceDto.setManager(managerDto);
		} catch (Exception e) {
			workspaceDto.setManager(null);
		}

		List<WorkspaceOperatorDto> operatorsDto = new ArrayList<WorkspaceOperatorDto>();
		for (WorkspaceOperatorEntity wOEntity : workspaceEntity.getOperators()) {
			WorkspaceOperatorDto workspaceOperatorDto = new WorkspaceOperatorDto();
			workspaceOperatorDto.setId(wOEntity.getId());
			workspaceOperatorDto.setCreatedAt(wOEntity.getCreatedAt());
			workspaceOperatorDto.setEndDate(wOEntity.getEndDate());
			workspaceOperatorDto.setNumberParcelsExpected(wOEntity.getNumberParcelsExpected());
			workspaceOperatorDto.setOperatorCode(wOEntity.getOperatorCode());
			workspaceOperatorDto.setStartDate(wOEntity.getStartDate());
			workspaceOperatorDto.setWorkArea(wOEntity.getWorkArea());
			workspaceOperatorDto.setObservations(wOEntity.getObservations());
			workspaceOperatorDto.setOperator(operatorDto);
			operatorsDto.add(workspaceOperatorDto);
		}

		workspaceDto.setOperators(operatorsDto);

		return workspaceDto;
	}

	public WorkspaceDto updateWorkspace(Long workspaceId, Date startDate, String observations,
			Long numberAlphanumericParcels, Double municipalityArea, Long managerCode) throws BusinessException {

		WorkspaceDto workspaceDto = null;

		// validate if the workspace exists
		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate if the workspace is active
		if (!workspaceEntity.getIsActive()) {
			throw new BusinessException(
					"No se puede asignar el operador porque el espacio de trabajo no es el actual.");
		}

		// validate access
		if (!managerCode.equals(workspaceEntity.getManagerCode())) {
			throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
		}

		workspaceEntity.setStartDate(startDate);
		workspaceEntity.setObservations(observations);
		workspaceEntity.setNumberAlphanumericParcels(numberAlphanumericParcels);
		workspaceEntity.setMunicipalityArea(municipalityArea);
		workspaceEntity.setUdpatedAt(new Date());

		workspaceEntity = workspaceService.updateWorkspace(workspaceEntity);

		workspaceDto = new WorkspaceDto();
		workspaceDto.setId(workspaceEntity.getId());
		workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
		workspaceDto.setUpdatedAt(workspaceEntity.getUdpatedAt());
		workspaceDto.setIsActive(workspaceEntity.getIsActive());
		workspaceDto.setManagerCode(workspaceEntity.getManagerCode());
		workspaceDto.setMunicipalityArea(workspaceEntity.getMunicipalityArea());
		workspaceDto.setNumberAlphanumericParcels(workspaceEntity.getNumberAlphanumericParcels());
		workspaceDto.setObservations(workspaceEntity.getObservations());
		workspaceDto.setStartDate(workspaceEntity.getStartDate());
		workspaceDto.setVersion(workspaceEntity.getVersion());
		workspaceDto.setState(new StateDto(workspaceEntity.getState().getId(), workspaceEntity.getState().getName(),
				workspaceEntity.getState().getDescription()));

		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(workspaceEntity.getManagerCode());
			workspaceDto.setManager(managerDto);
		} catch (Exception e) {
			workspaceDto.setManager(null);
		}

		List<WorkspaceOperatorDto> operatorsDto = new ArrayList<WorkspaceOperatorDto>();
		for (WorkspaceOperatorEntity wOEntity : workspaceEntity.getOperators()) {
			WorkspaceOperatorDto workspaceOperatorDto = new WorkspaceOperatorDto();
			workspaceOperatorDto.setId(wOEntity.getId());
			workspaceOperatorDto.setCreatedAt(wOEntity.getCreatedAt());
			workspaceOperatorDto.setEndDate(wOEntity.getEndDate());
			workspaceOperatorDto.setNumberParcelsExpected(wOEntity.getNumberParcelsExpected());
			workspaceOperatorDto.setOperatorCode(wOEntity.getOperatorCode());
			workspaceOperatorDto.setStartDate(wOEntity.getStartDate());
			workspaceOperatorDto.setWorkArea(wOEntity.getWorkArea());
			workspaceOperatorDto.setObservations(wOEntity.getObservations());

			// get operator
			try {
				MicroserviceOperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
				workspaceOperatorDto.setOperator(operatorDto);
			} catch (Exception e) {
				workspaceOperatorDto.setOperator(null);
			}
			operatorsDto.add(workspaceOperatorDto);
		}

		workspaceDto.setOperators(operatorsDto);

		return workspaceDto;
	}

	public WorkspaceEntity cloneWorkspace(Long workspaceId, Long fromClone) throws BusinessException {

		WorkspaceEntity cloneWorkspaceEntity = null;

		List<Long> supportsToSkip = new ArrayList<Long>();
		if (fromClone.equals(WorkspaceBusiness.WORKSPACE_CLONE_FROM_CHANGE_MANAGER)) {
			supportsToSkip.add(MilestoneBusiness.MILESTONE_NEW_WORKSPACE);
			supportsToSkip.add(MilestoneBusiness.MILESTONE_OPERATOR_ASSIGNMENT);
		} else if (fromClone.equals(WorkspaceBusiness.WORKSPACE_CLONE_FROM_CHANGE_OPERATOR)) {
			supportsToSkip.add(MilestoneBusiness.MILESTONE_OPERATOR_ASSIGNMENT);
		}

		WorkspaceEntity workspaceEntityFound = workspaceService.getWorkspaceById(workspaceId);
		if (workspaceEntityFound instanceof WorkspaceEntity) {

			Long countWorkspaces = workspaceService.getCountByMunicipality(workspaceEntityFound.getMunicipality());

			cloneWorkspaceEntity = new WorkspaceEntity();
			cloneWorkspaceEntity.setCreatedAt(new Date());
			cloneWorkspaceEntity.setIsActive(true);
			cloneWorkspaceEntity.setManagerCode(workspaceEntityFound.getManagerCode());
			cloneWorkspaceEntity.setObservations(workspaceEntityFound.getObservations());
			cloneWorkspaceEntity.setNumberAlphanumericParcels(workspaceEntityFound.getNumberAlphanumericParcels());
			cloneWorkspaceEntity.setMunicipalityArea(workspaceEntityFound.getMunicipalityArea());
			cloneWorkspaceEntity.setStartDate(workspaceEntityFound.getStartDate());
			cloneWorkspaceEntity.setVersion(countWorkspaces + 1);
			cloneWorkspaceEntity.setMunicipality(workspaceEntityFound.getMunicipality());
			cloneWorkspaceEntity.setState(workspaceEntityFound.getState());
			cloneWorkspaceEntity.setWorkspace(workspaceEntityFound);

			// clone states history
			List<WorkspaceStateEntity> statesHistory = new ArrayList<WorkspaceStateEntity>();
			List<WorkspaceStateEntity> listStates = workspaceEntityFound.getStatesHistory();
			for (WorkspaceStateEntity wStateEntity : listStates) {
				WorkspaceStateEntity stateNewEntity = new WorkspaceStateEntity();
				stateNewEntity.setCreatedAt(wStateEntity.getCreatedAt());
				stateNewEntity.setState(wStateEntity.getState());
				stateNewEntity.setWorkspace(cloneWorkspaceEntity);
				statesHistory.add(stateNewEntity);
			}

			cloneWorkspaceEntity.setStatesHistory(statesHistory);

			// clone supports
			List<SupportEntity> supports = new ArrayList<SupportEntity>();
			List<SupportEntity> supportsFound = workspaceEntityFound.getSupports();
			for (SupportEntity supportEntity : supportsFound) {
				if (!supportsToSkip.contains(supportEntity.getMilestone().getId())) {
					SupportEntity supportNewEntity = new SupportEntity();
					supportNewEntity.setCreatedAt(supportEntity.getCreatedAt());
					supportNewEntity.setUrlDocumentaryRepository(supportEntity.getUrlDocumentaryRepository());
					supportNewEntity.setWorkspace(cloneWorkspaceEntity);
					supportNewEntity.setMilestone(supportEntity.getMilestone());
					supports.add(supportNewEntity);
				}
			}
			cloneWorkspaceEntity.setSupports(supports);

			cloneWorkspaceEntity = workspaceService.createWorkspace(cloneWorkspaceEntity);

			// set workspace old to inactive
			workspaceEntityFound.setIsActive(false);
			workspaceEntityFound = workspaceService.updateWorkspace(workspaceEntityFound);
		}

		return cloneWorkspaceEntity;
	}

	public List<SupportDto> getSupportsByWorkspaceId(Long workspaceId, Long managerCode) throws BusinessException {

		List<SupportDto> listSupportsDto = new ArrayList<SupportDto>();

		// validate if the workspace exists
		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate access
		if (managerCode != null) {
			if (!managerCode.equals(workspaceEntity.getManagerCode())) {
				throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
			}
		}

		List<SupportEntity> listSupportsEntity = workspaceEntity.getSupports();

		for (SupportEntity supportEntity : listSupportsEntity) {
			SupportDto supportDto = new SupportDto();
			supportDto.setId(supportEntity.getId());
			supportDto.setCreatedAt(supportEntity.getCreatedAt());
			supportDto.setUrlDocumentaryRepository(supportEntity.getUrlDocumentaryRepository());

			WorkspaceDto workspaceDto = new WorkspaceDto();
			workspaceDto.setId(workspaceEntity.getId());
			workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
			workspaceDto.setUpdatedAt(workspaceEntity.getUdpatedAt());
			workspaceDto.setIsActive(workspaceEntity.getIsActive());
			workspaceDto.setManagerCode(workspaceEntity.getManagerCode());
			workspaceDto.setMunicipalityArea(workspaceEntity.getMunicipalityArea());
			workspaceDto.setNumberAlphanumericParcels(workspaceEntity.getNumberAlphanumericParcels());
			workspaceDto.setObservations(workspaceEntity.getObservations());
			workspaceDto.setStartDate(workspaceEntity.getStartDate());
			workspaceDto.setVersion(workspaceEntity.getVersion());
			workspaceDto.setState(new StateDto(workspaceEntity.getState().getId(), workspaceEntity.getState().getName(),
					workspaceEntity.getState().getDescription()));

			supportDto.setMilestone(
					new MilestoneDto(supportEntity.getMilestone().getId(), supportEntity.getMilestone().getName()));
			supportDto.setWorkspace(workspaceDto);

			listSupportsDto.add(supportDto);
		}

		return listSupportsDto;
	}

	public WorkspaceDto getWorkspaceById(Long workspaceId, Long managerCode) throws BusinessException {

		// validate if the workspace exists
		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate access
		if (managerCode != null) {
			if (!managerCode.equals(workspaceEntity.getManagerCode())) {
				throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
			}
		}

		WorkspaceDto workspaceDto = new WorkspaceDto();
		workspaceDto.setId(workspaceEntity.getId());
		workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
		workspaceDto.setUpdatedAt(workspaceEntity.getUdpatedAt());
		workspaceDto.setIsActive(workspaceEntity.getIsActive());
		workspaceDto.setManagerCode(workspaceEntity.getManagerCode());
		workspaceDto.setMunicipalityArea(workspaceEntity.getMunicipalityArea());
		workspaceDto.setNumberAlphanumericParcels(workspaceEntity.getNumberAlphanumericParcels());
		workspaceDto.setObservations(workspaceEntity.getObservations());
		workspaceDto.setStartDate(workspaceEntity.getStartDate());
		workspaceDto.setVersion(workspaceEntity.getVersion());
		workspaceDto.setState(new StateDto(workspaceEntity.getState().getId(), workspaceEntity.getState().getName(),
				workspaceEntity.getState().getDescription()));

		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(workspaceEntity.getManagerCode());
			workspaceDto.setManager(managerDto);
		} catch (Exception e) {
			workspaceDto.setManager(null);
		}

		List<WorkspaceOperatorDto> operatorsDto = new ArrayList<WorkspaceOperatorDto>();

		for (WorkspaceOperatorEntity wOEntity : workspaceEntity.getOperators()) {
			WorkspaceOperatorDto workspaceOperatorDto = new WorkspaceOperatorDto();
			workspaceOperatorDto.setId(wOEntity.getId());
			workspaceOperatorDto.setCreatedAt(wOEntity.getCreatedAt());
			workspaceOperatorDto.setEndDate(wOEntity.getEndDate());
			workspaceOperatorDto.setNumberParcelsExpected(wOEntity.getNumberParcelsExpected());
			workspaceOperatorDto.setOperatorCode(wOEntity.getOperatorCode());
			workspaceOperatorDto.setStartDate(wOEntity.getStartDate());
			workspaceOperatorDto.setWorkArea(wOEntity.getWorkArea());
			workspaceOperatorDto.setObservations(wOEntity.getObservations());

			// get operator
			try {
				MicroserviceOperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
				workspaceOperatorDto.setOperator(operatorDto);
			} catch (Exception e) {
				workspaceOperatorDto.setOperator(null);
			}
			operatorsDto.add(workspaceOperatorDto);
		}

		workspaceDto.setOperators(operatorsDto);

		MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();
		DepartmentEntity deparmentEntity = municipalityEntity.getDepartment();

		MunicipalityDto municipalityDto = new MunicipalityDto();
		municipalityDto.setCode(municipalityEntity.getCode());
		municipalityDto.setId(municipalityEntity.getId());
		municipalityDto.setName(municipalityEntity.getName());
		municipalityDto.setDepartment(
				new DepartmentDto(deparmentEntity.getId(), deparmentEntity.getName(), deparmentEntity.getCode()));

		workspaceDto.setMunicipality(municipalityDto);

		return workspaceDto;
	}

	public List<WorkspaceOperatorDto> getOperatorsByWorkspaceId(Long workspaceId, Long managerCode)
			throws BusinessException {

		List<WorkspaceOperatorDto> listOperatorsDto = new ArrayList<WorkspaceOperatorDto>();

		// validate if the workspace exists
		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate access
		if (managerCode != null) {
			if (!managerCode.equals(workspaceEntity.getManagerCode())) {
				throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
			}
		}

		for (WorkspaceOperatorEntity wOEntity : workspaceEntity.getOperators()) {
			WorkspaceOperatorDto workspaceOperatorDto = new WorkspaceOperatorDto();
			workspaceOperatorDto.setId(wOEntity.getId());
			workspaceOperatorDto.setCreatedAt(wOEntity.getCreatedAt());
			workspaceOperatorDto.setEndDate(wOEntity.getEndDate());
			workspaceOperatorDto.setNumberParcelsExpected(wOEntity.getNumberParcelsExpected());
			workspaceOperatorDto.setOperatorCode(wOEntity.getOperatorCode());
			workspaceOperatorDto.setStartDate(wOEntity.getStartDate());
			workspaceOperatorDto.setWorkArea(wOEntity.getWorkArea());
			workspaceOperatorDto.setObservations(wOEntity.getObservations());

			// get operator
			try {
				MicroserviceOperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
				workspaceOperatorDto.setOperator(operatorDto);
			} catch (Exception e) {
				workspaceOperatorDto.setOperator(null);
			}
			listOperatorsDto.add(workspaceOperatorDto);
		}

		return listOperatorsDto;
	}

	public WorkspaceDto getWorkspaceActiveByMunicipality(Long municipalityId, Long managerCode)
			throws BusinessException {

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("El municipio no existe.");
		}

		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);

		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No existe un espacio de trabajo para el municipio.");
		}

		// validate access
		if (managerCode != null) {
			if (!managerCode.equals(workspaceEntity.getManagerCode())) {
				throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
			}
		}

		WorkspaceDto workspaceDto = new WorkspaceDto();
		workspaceDto.setId(workspaceEntity.getId());
		workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
		workspaceDto.setUpdatedAt(workspaceEntity.getUdpatedAt());
		workspaceDto.setIsActive(workspaceEntity.getIsActive());
		workspaceDto.setManagerCode(workspaceEntity.getManagerCode());
		workspaceDto.setMunicipalityArea(workspaceEntity.getMunicipalityArea());
		workspaceDto.setNumberAlphanumericParcels(workspaceEntity.getNumberAlphanumericParcels());
		workspaceDto.setObservations(workspaceEntity.getObservations());
		workspaceDto.setStartDate(workspaceEntity.getStartDate());
		workspaceDto.setVersion(workspaceEntity.getVersion());
		workspaceDto.setState(new StateDto(workspaceEntity.getState().getId(), workspaceEntity.getState().getName(),
				workspaceEntity.getState().getDescription()));

		MicroserviceManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(workspaceEntity.getManagerCode());
			workspaceDto.setManager(managerDto);
		} catch (Exception e) {
			workspaceDto.setManager(null);
		}

		List<WorkspaceOperatorDto> operatorsDto = new ArrayList<WorkspaceOperatorDto>();

		for (WorkspaceOperatorEntity wOEntity : workspaceEntity.getOperators()) {
			WorkspaceOperatorDto workspaceOperatorDto = new WorkspaceOperatorDto();
			workspaceOperatorDto.setId(wOEntity.getId());
			workspaceOperatorDto.setCreatedAt(wOEntity.getCreatedAt());
			workspaceOperatorDto.setEndDate(wOEntity.getEndDate());
			workspaceOperatorDto.setNumberParcelsExpected(wOEntity.getNumberParcelsExpected());
			workspaceOperatorDto.setOperatorCode(wOEntity.getOperatorCode());
			workspaceOperatorDto.setStartDate(wOEntity.getStartDate());
			workspaceOperatorDto.setWorkArea(wOEntity.getWorkArea());
			workspaceOperatorDto.setObservations(wOEntity.getObservations());

			// get operator
			try {
				MicroserviceOperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
				workspaceOperatorDto.setOperator(operatorDto);
			} catch (FeignException e) {
				workspaceOperatorDto.setOperator(null);
			}
			operatorsDto.add(workspaceOperatorDto);
		}

		workspaceDto.setOperators(operatorsDto);

		return workspaceDto;
	}

	public List<MicroserviceRequestDto> createRequest(Date deadline, List<TypeSupplyRequestedDto> supplies,
			Long userCode, Long managerCode, Long municipalityId) throws BusinessException {

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("El municipio no existe.");
		}

		// validate access
		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
		if (workspaceEntity instanceof WorkspaceEntity) {
			if (!managerCode.equals(workspaceEntity.getManagerCode())) {
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

		List<MicroserviceCreateRequestDto> groupRequests = new ArrayList<MicroserviceCreateRequestDto>();
		List<Long> skipped = new ArrayList<Long>();

		String packageLabel = RandomStringUtils.random(4, false, true).toLowerCase() + new Date().getTime();

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
				List<MicroserviceTypeSupplyRequestedDto> listSuppliesByProfile = new ArrayList<MicroserviceTypeSupplyRequestedDto>();
				for (TypeSupplyRequestedDto supplyDto2 : supplies) {

					MicroserviceTypeSupplyDto typeSupplyDto2 = providerClient
							.findTypeSuppleById(supplyDto2.getTypeSupplyId());

					if (typeSupplyDto2.getProviderProfile().getId().equals(profileId)) {

						if (typeSupplyDto2.getModelRequired()
								&& (supplyDto2.getModelVersion() == null || supplyDto2.getModelVersion().isEmpty())) {
							throw new BusinessException(
									"El tipo de insumo solicita que se especifique una versión del modelo.");
						}

						MicroserviceTypeSupplyRequestedDto mtsr = new MicroserviceTypeSupplyRequestedDto();
						mtsr.setObservation(supplyDto2.getObservation());
						mtsr.setTypeSupplyId(supplyDto2.getTypeSupplyId());
						mtsr.setModelVersion(supplyDto2.getModelVersion());
						listSuppliesByProfile.add(mtsr);
					}
				}

				// emitters by request
				List<MicroserviceRequestEmitterDto> listEmittersByProvider = new ArrayList<MicroserviceRequestEmitterDto>();
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

		List<MicroserviceRequestDto> requests = new ArrayList<MicroserviceRequestDto>();
		for (MicroserviceCreateRequestDto request : groupRequests) {

			try {

				MicroserviceRequestDto responseRequest = providerClient.createRequest(request);
				requests.add(responseRequest);

				// send notification
				try {

					List<MicroserviceProviderUserDto> providerUsers = providerBusiness
							.getUsersByProvider(request.getProviderId(), null);

					Long providerProfileId = responseRequest.getSuppliesRequested().get(0).getTypeSupply()
							.getProviderProfile().getId();

					for (MicroserviceProviderUserDto providerUser : providerUsers) {

						MicroserviceUserDto userDto = userBusiness.getUserById(providerUser.getUserCode());

						MicroserviceProviderProfileDto profileFound = providerUser.getProfiles().stream()
								.filter(p -> p.getId().equals(providerProfileId)).findAny().orElse(null);

						if (profileFound != null) {
							notificationBusiness.sendNotificationInputRequest(userDto.getEmail(), userCode,
									managerDto.getName(), municipalityEntity.getName(),
									municipalityEntity.getDepartment().getName(), responseRequest.getId().toString(),
									new Date());
						}
					}

				} catch (Exception er) {
					log.error("Error enviando la notificación por solicitud de insumos: " + er.getMessage());
				}

				if (responseRequest.getProvider().getId().equals(ProviderBusiness.PROVIDER_IGAC_ID)) {

					MicroserviceSupplyRequestedDto supplyRequested = responseRequest.getSuppliesRequested().stream()
							.filter(sR -> sR.getTypeSupply().getId().equals(ProviderBusiness.PROVIDER_SUPPLY_CADASTRAL))
							.findAny().orElse(null);

					if (supplyRequested instanceof MicroserviceSupplyRequestedDto) {
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

	public List<MicroserviceRequestDto> getPendingRequestByProvider(Long userCode, Long providerId)
			throws BusinessException {

		List<MicroserviceRequestDto> listPendingRequestsDto = new ArrayList<MicroserviceRequestDto>();

		try {

			List<MicroserviceProviderUserDto> usersByProvider = providerClient.findUsersByProviderId(providerId);
			MicroserviceProviderUserDto userProviderFound = usersByProvider.stream()
					.filter(user -> userCode.equals(user.getUserCode())).findAny().orElse(null);
			if (userProviderFound == null) {
				throw new BusinessException("El usuario no esta registrado como usuario para el proveedor de insumo.");
			}

			List<MicroserviceRequestDto> responseRequestsDto = providerClient.getRequestsByProvider(providerId,
					ProviderBusiness.REQUEST_STATE_REQUESTED);

			for (MicroserviceRequestDto requestDto : responseRequestsDto) {

				List<MicroserviceEmitterDto> emittersDto = new ArrayList<MicroserviceEmitterDto>();
				for (MicroserviceEmitterDto emitterDto : requestDto.getEmitters()) {
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

				for (MicroserviceSupplyRequestedDto supply : requestDto.getSuppliesRequested()) {

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
				List<MicroserviceSupplyRequestedDto> suppliesRequested = new ArrayList<>();

				int countNot = 0;

				for (MicroserviceSupplyRequestedDto supply : requestDto.getSuppliesRequested()) {

					MicroserviceProviderProfileDto profileSupply = supply.getTypeSupply().getProviderProfile();

					MicroserviceProviderProfileDto profileUser = userProviderFound.getProfiles().stream()
							.filter(profile -> profileSupply.getId().equals(profile.getId())).findAny().orElse(null);

					if (profileUser != null) {
						supply.setCanUpload(true);
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

	public List<MicroserviceRequestDto> getClosedRequestByProvider(Long userCode, Long providerId)
			throws BusinessException {

		List<MicroserviceRequestDto> listClosedRequestsDto = new ArrayList<MicroserviceRequestDto>();

		try {

			List<MicroserviceRequestDto> responseRequestsDto = providerClient.getRequestsByProviderClosed(providerId,
					userCode);

			for (MicroserviceRequestDto requestDto : responseRequestsDto) {

				List<MicroserviceEmitterDto> emittersDto = new ArrayList<MicroserviceEmitterDto>();
				for (MicroserviceEmitterDto emitterDto : requestDto.getEmitters()) {
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

				for (MicroserviceSupplyRequestedDto supply : requestDto.getSuppliesRequested()) {

					log.info("description: " + supply.getDescription());

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
			Long supplyIdRegistration, MicroserviceManagerDto managerDto, MicroserviceUserDto userDto)
			throws BusinessException {

		IntegrationDto integrationResponseDto = null;

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("El municipio no existe.");
		}

		WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
		if (!(workspaceActive instanceof WorkspaceEntity)) {
			throw new BusinessException("El municipio no cuenta con un espacio de trabajo activo.");
		}

		if (!managerDto.getId().equals(workspaceActive.getManagerCode())) {
			throw new BusinessException("No tiene acceso al municipio.");
		}

		// validate if there is an integration running
		List<Long> statesId = new ArrayList<>();
		statesId.add(IntegrationStateBusiness.STATE_STARTED_AUTOMATIC);
		statesId.add(IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC);
		statesId.add(IntegrationStateBusiness.STATE_STARTED_ASSISTED);
		statesId.add(IntegrationStateBusiness.STATE_FINISHED_ASSISTED);
		List<IntegrationEntity> integrationsPending = integrationService
				.getIntegrationByWorkspaceAndStates(workspaceActive.getId(), statesId);
		if (integrationsPending.size() > 0) {
			throw new BusinessException("Existe una integración en curso para el municipio.");
		}

		// validate cadastre
		MicroserviceSupplyDto supplyCadastreDto = null;
		String pathFileCadastre = null;
		try {

			supplyCadastreDto = supplyClient.findSupplyById(supplyIdCadastre);

			MicroserviceTypeSupplyDto typeSupplyDto = providerClient
					.findTypeSuppleById(supplyCadastreDto.getTypeSupplyCode());

			supplyCadastreDto.setTypeSupply(typeSupplyDto);

			MicroserviceSupplyAttachmentDto attachment = supplyCadastreDto.getAttachments().get(0);
			pathFileCadastre = attachment.getUrlDocumentaryRepository();

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
		MicroserviceSupplyDto supplyRegisteredDto = null;
		String pathFileRegistration = null;
		try {

			supplyRegisteredDto = supplyClient.findSupplyById(supplyIdRegistration);

			MicroserviceTypeSupplyDto typeSupplyDto = providerClient
					.findTypeSuppleById(supplyRegisteredDto.getTypeSupplyCode());

			supplyRegisteredDto.setTypeSupply(typeSupplyDto);

			MicroserviceSupplyAttachmentDto attachment = supplyRegisteredDto.getAttachments().get(0);
			pathFileRegistration = attachment.getUrlDocumentaryRepository();

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
				supplyCadastreDto.getId(), supplyRegisteredDto.getId(), stateGeneratedProduct);
		if (integrationDone instanceof IntegrationEntity) {
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

		Long integrationId = null;
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
					IntegrationStateBusiness.STATE_ERROR_INTEGRATION_AUTOMATIC, null, null, "SISTEMA");
			log.error("No se ha podido iniciar la integración: " + e.getMessage());
			throw new BusinessException("No se ha podido iniciar la integración.");
		}

		return integrationResponseDto;
	}

	public IntegrationDto startIntegrationAssisted(Long workspaceId, Long integrationId,
			MicroserviceManagerDto managerDto, MicroserviceUserDto userDto) throws BusinessException {

		IntegrationDto integrationDto = null;

		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate if the workspace is active
		if (!workspaceEntity.getIsActive()) {
			throw new BusinessException(
					"No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
		}

		if (!managerDto.getId().equals(workspaceEntity.getManagerCode())) {
			throw new BusinessException("No tiene acceso al municipio.");
		}

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
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
				IntegrationStateBusiness.STATE_STARTED_ASSISTED, userDto.getId(), managerDto.getId(), textHistory);

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

					MicroserviceUserDto userIntegratorDto = userBusiness.getUserById(managerUserDto.getUserCode());
					if (userIntegratorDto instanceof MicroserviceUserDto) {
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

		IntegrationDto integrationDto = null;

		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate if the workspace is active
		if (!workspaceEntity.getIsActive()) {
			throw new BusinessException(
					"No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
		}

		if (!managerDto.getId().equals(workspaceEntity.getManagerCode())) {
			throw new BusinessException("No tiene acceso al municipio.");
		}

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
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
			MicroserviceSupplyDto supplyCadastreDto = supplyClient
					.findSupplyById(integrationEntity.getSupplyCadastreId());

			String urlBase = "/" + workspaceEntity.getMunicipality().getCode().replace(" ", "_") + "/insumos/gestores/"
					+ workspaceEntity.getManagerCode();

			iliBusiness.startExport(hostnameDecrypt, databaseDecrypt, databaseIntegrationPassword, portDecrypt,
					schemaDecrypt, databaseIntegrationUsername, integrationId, false,
					supplyCadastreDto.getModelVersion(), urlBase);

			// modify integration state
			String textHistory = userDto.getFirstName() + " " + userDto.getLastName() + " - " + managerDto.getName();
			integrationDto = integrationBusiness.updateStateToIntegration(integrationId,
					IntegrationStateBusiness.STATE_GENERATING_PRODUCT, userDto.getId(), managerDto.getId(),
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
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate if the workspace is active
		if (!workspaceEntity.getIsActive()) {
			throw new BusinessException(
					"No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
		}

		if (!managerCode.equals(workspaceEntity.getManagerCode())) {
			throw new BusinessException("No tiene acceso al municipio.");
		}

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
			throw new BusinessException("No se ha encontrado la integración.");
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

		Boolean hasAccess = false;

		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityByCode(municipalityCode);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			return hasAccess;
		}

		WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
		if (workspaceActive instanceof WorkspaceEntity) {
			if (managerCode.equals(workspaceActive.getManagerCode())) {
				hasAccess = true;
			}
		}

		return hasAccess;
	}

	public void removeSupply(Long workspaceId, Long supplyId, Long managerCode) throws BusinessException {

		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate if the workspace is active
		if (!workspaceEntity.getIsActive()) {
			throw new BusinessException(
					"No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
		}

		if (!managerCode.equals(workspaceEntity.getManagerCode())) {
			throw new BusinessException("No tiene acceso al municipio.");
		}

		MicroserviceSupplyDto supplyDto = null;
		String pathFile = null;
		try {

			supplyDto = supplyClient.findSupplyById(supplyId);

			MicroserviceSupplyAttachmentDto attachment = supplyDto.getAttachments().get(0);
			pathFile = attachment.getUrlDocumentaryRepository();

		} catch (Exception e) {
			log.error("No se ha encontrado el insumo para eliminarlo: " + e.getMessage());
			throw new BusinessException("No se ha encontrado el insumo.");
		}

		MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

		if (!supplyDto.getMunicipalityCode().equals(municipalityEntity.getCode())) {
			throw new BusinessException("El insumo no pertenece al municipio.");
		}

		supplyBusiness.deleteSupply(supplyId);
		fileBusiness.deleteFile(pathFile);
	}

	public MicroserviceDeliveryDto createDelivery(Long workspaceId, Long managerCode, String observations,
			List<CreateSupplyDeliveryDto> suppliesDto) throws BusinessException {

		MicroserviceDeliveryDto deliveryDto = null;

		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate if the workspace is active
		if (!workspaceEntity.getIsActive()) {
			throw new BusinessException(
					"No se puede iniciar la integración ya que le espacio de trabajo no es el actual.");
		}

		if (!managerCode.equals(workspaceEntity.getManagerCode())) {
			throw new BusinessException("No tiene acceso al municipio.");
		}

		List<WorkspaceOperatorEntity> operators = workspaceEntity.getOperators();
		if (operators.size() == 0) {
			throw new BusinessException("El espacio de trabajo no tiene asignado un operador.");
		}

		Long operatorId = operators.get(0).getOperatorCode();
		MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

		List<MicroserviceCreateDeliverySupplyDto> microserviceSupplies = new ArrayList<>();

		// verify if the supplies exists
		List<MicroserviceDeliveryDto> deliveriesDto = operatorBusiness.getDeliveriesByOperator(operatorId,
				municipalityEntity.getCode());

		for (CreateSupplyDeliveryDto deliverySupplyDto : suppliesDto) {

			MicroserviceSupplyDto supply = supplyBusiness.getSupplyById(deliverySupplyDto.getSupplyId());
			if (supply == null) {
				throw new BusinessException("No se ha encontrado el insumo.");
			}

			// verify if the supply has already delivered to operator
			for (MicroserviceDeliveryDto deliveryFoundDto : deliveriesDto) {
				MicroserviceSupplyDeliveryDto supplyFound = deliveryFoundDto.getSupplies().stream()
						.filter(supplyDto -> supplyDto.getSupplyCode().equals(deliverySupplyDto.getSupplyId()))
						.findAny().orElse(null);
				if (supplyFound != null) {

					String nameSupply = "";
					if (supply.getTypeSupply() != null) {
						nameSupply = supply.getTypeSupply().getName();
					} else {
						nameSupply = supply.getObservations();
					}

					String messageError = String.format("El insumo %s ya ha sido entregado al operador.", nameSupply);
					throw new BusinessException(messageError);
				}
			}

			microserviceSupplies.add(new MicroserviceCreateDeliverySupplyDto(deliverySupplyDto.getObservations(),
					deliverySupplyDto.getSupplyId()));
		}

		try {
			deliveryDto = operatorBusiness.createDelivery(operatorId, managerCode, municipalityEntity.getCode(),
					observations, microserviceSupplies);

			try {

				MicroserviceManagerDto managerDto = managerBusiness.getManagerById(managerCode);

				List<MicroserviceOperatorUserDto> operatorUsers = operatorBusiness.getUsersByOperator(operatorId);
				for (MicroserviceOperatorUserDto operatorUser : operatorUsers) {
					MicroserviceUserDto userDto = userBusiness.getUserById(operatorUser.getUserCode());
					if (userDto instanceof MicroserviceUserDto) {
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

	public SupportDto getSupportByIdToDownload(Long workspaceId, Long supportId, Long managerCode)
			throws BusinessException {

		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceById(workspaceId);
		if (!(workspaceEntity instanceof WorkspaceEntity)) {
			throw new BusinessException("No se ha encontrado el espacio de trabajo.");
		}

		// validate if the workspace is active
		if (!workspaceEntity.getIsActive()) {
			throw new BusinessException(
					"No se puede descargar el soporte, porque el municipio no tiene un espacio de trabajo activo.");
		}

		if (managerCode != null) {
			if (!managerCode.equals(workspaceEntity.getManagerCode())) {
				throw new BusinessException("No tiene acceso al municipio.");
			}
		}

		SupportEntity supportEntity = workspaceEntity.getSupports().stream().filter(s -> s.getId().equals(supportId))
				.findAny().orElse(null);
		if (supportEntity == null) {
			throw new BusinessException("El soporte no pertenece al espacio de trabajo.");
		}

		SupportDto supportDto = new SupportDto();
		supportDto.setId(supportEntity.getId());
		supportDto.setCreatedAt(supportEntity.getCreatedAt());
		supportDto.setUrlDocumentaryRepository(supportEntity.getUrlDocumentaryRepository());
		return supportDto;
	}

}
