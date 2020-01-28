package com.ai.st.microservice.workspaces.business;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.IliFeignClient;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.OperatorFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
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
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIli2pgExportDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationCadastreRegistrationDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.workspaces.dto.operators.OperatorDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceEmitterDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestEmitterDto;
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
import com.ai.st.microservice.workspaces.services.RabbitMQSenderService;

import feign.FeignException;

@Component
public class WorkspaceBusiness {

	@Value("${integrations.database.hostname}")
	private String databaseIntegrationHost;

	@Value("${integrations.database.port}")
	private String databaseIntegrationPort;

	@Value("${integrations.database.schema}")
	private String databaseIntegrationSchema;

	@Value("${st.temporalDirectory}")
	private String stTemporalDirectory;

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
	private IliFeignClient iliClient;

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
	private RabbitMQSenderService rabbitMQSenderService;

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

		// save file with microservice file manager
		String urlDocumentaryRepository = null;
		try {
			String tmpFile = this.stTemporalDirectory + File.separatorChar
					+ StringUtils.cleanPath(supportFile.getOriginalFilename());

			FileUtils.writeByteArrayToFile(new File(tmpFile), supportFile.getBytes());

			String urlBase = "/" + municipalityEntity.getCode() + "/soportes/gestores";

			urlDocumentaryRepository = rabbitMQSenderService
					.sendFile(StringUtils.cleanPath(supportFile.getOriginalFilename()), urlBase);

		} catch (IOException e) {
			throw new BusinessException("No se ha podido cargar el soporte.");
		}

		if (urlDocumentaryRepository == null) {
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
				if (codeManager != workspaceActive.getManagerCode()) {
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
					OperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
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
		if (managerCode != workspaceEntity.getManagerCode()) {
			throw new BusinessException("El usuario no tiene acceso al espacio de trabajo.");
		}

		// validate if the operator exists
		OperatorDto operatorDto = null;
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

		// save file with microservice file manager
		String urlDocumentaryRepository = null;
		try {

			String tmpFile = this.stTemporalDirectory + File.separatorChar
					+ StringUtils.cleanPath(supportFile.getOriginalFilename());

			FileUtils.writeByteArrayToFile(new File(tmpFile), supportFile.getBytes());

			String urlBase = "/" + workspaceEntity.getMunicipality().getCode() + "/soportes/operadores";

			urlDocumentaryRepository = rabbitMQSenderService
					.sendFile(StringUtils.cleanPath(supportFile.getOriginalFilename()), urlBase);

		} catch (IOException e) {
			throw new BusinessException("No se ha podido cargar el soporte.");
		}

		if (urlDocumentaryRepository == null) {
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
		if (managerCode != workspaceEntity.getManagerCode()) {
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
				OperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
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
		if (fromClone == WorkspaceBusiness.WORKSPACE_CLONE_FROM_CHANGE_MANAGER) {
			supportsToSkip.add(MilestoneBusiness.MILESTONE_NEW_WORKSPACE);
			supportsToSkip.add(MilestoneBusiness.MILESTONE_OPERATOR_ASSIGNMENT);
		} else if (fromClone == WorkspaceBusiness.WORKSPACE_CLONE_FROM_CHANGE_OPERATOR) {
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
			if (managerCode != workspaceEntity.getManagerCode()) {
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
			if (managerCode != workspaceEntity.getManagerCode()) {
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
				OperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
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
			if (managerCode != workspaceEntity.getManagerCode()) {
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
				OperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
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
			if (managerCode != workspaceEntity.getManagerCode()) {
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
				OperatorDto operatorDto = operatorClient.findById(wOEntity.getOperatorCode());
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
			if (managerCode != workspaceEntity.getManagerCode()) {
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

		for (TypeSupplyRequestedDto supplyDto : supplies) {

			Long providerId = supplyDto.getProviderId();

			if (!skipped.contains(providerId)) {

				MicroserviceCreateRequestDto requestDto = new MicroserviceCreateRequestDto();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				requestDto.setDeadline(sdf.format(deadline));
				requestDto.setProviderId(providerId);
				requestDto.setMunicipalityCode(municipalityEntity.getCode());

				// supplies by request
				List<MicroserviceTypeSupplyRequestedDto> listSuppliesByProvider = new ArrayList<MicroserviceTypeSupplyRequestedDto>();
				for (TypeSupplyRequestedDto supplyDto2 : supplies) {
					if (supplyDto2.getProviderId() == providerId) {
						MicroserviceTypeSupplyRequestedDto mtsr = new MicroserviceTypeSupplyRequestedDto();
						mtsr.setObservation(supplyDto2.getObservation());
						mtsr.setTypeSupplyId(supplyDto2.getTypeSupplyId());
						listSuppliesByProvider.add(mtsr);
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

				requestDto.setSupplies(listSuppliesByProvider);
				requestDto.setEmitters(listEmittersByProvider);

				groupRequests.add(requestDto);
				skipped.add(providerId);
			}

		}

		List<MicroserviceRequestDto> requests = new ArrayList<MicroserviceRequestDto>();
		for (MicroserviceCreateRequestDto request : groupRequests) {

			try {
				MicroserviceRequestDto responseRequest = providerClient.createRequest(request);
				requests.add(responseRequest);
			} catch (Exception e) {

			}

		}

		return requests;
	}

	public List<MicroserviceRequestDto> getPendingRequestByProvider(Long providerId) throws BusinessException {

		List<MicroserviceRequestDto> listPendingRequestsDto = new ArrayList<MicroserviceRequestDto>();

		try {
			List<MicroserviceRequestDto> responseRequestsDto = providerClient.getRequestsByProvider(providerId,
					(long) 1);

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

				listPendingRequestsDto.add(requestDto);
			}

		} catch (Exception e) {

		}

		return listPendingRequestsDto;
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

		if (managerDto.getId() != workspaceActive.getManagerCode()) {
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
			throw new BusinessException("No se ha podido crear la integración.");
		}

		try {
			MicroserviceIntegrationCadastreRegistrationDto integrationDto = new MicroserviceIntegrationCadastreRegistrationDto();

			integrationDto.setCadastrePathXTF(pathFileCadastre);
			integrationDto.setRegistrationPathXTF(pathFileRegistration);
			integrationDto.setDatabaseHost(databaseIntegrationHost);
			integrationDto.setDatabaseName(randomDatabaseName);
			integrationDto.setDatabasePassword(randomPassword);
			integrationDto.setDatabasePort(databaseIntegrationPort);
			integrationDto.setDatabaseSchema(databaseIntegrationSchema);
			integrationDto.setDatabaseUsername(randomUsername);
			integrationDto.setIntegrationId(integrationId);

			iliClient.startIntegrationCadastreRegistration(integrationDto);

		} catch (Exception e) {
			integrationBusiness.updateStateToIntegration(integrationId,
					IntegrationStateBusiness.STATE_ERROR_INTEGRATION_AUTOMATIC, null, null, "SISTEMA");
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

		if (managerDto.getId() != workspaceEntity.getManagerCode()) {
			throw new BusinessException("No tiene acceso al municipio.");
		}

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
			throw new BusinessException("No se ha encontrado la integración.");
		}

		if (integrationEntity.getWorkspace().getId() != workspaceEntity.getId()) {
			throw new BusinessException("La integración no pertenece al espacio de trabajo.");
		}

		IntegrationStateEntity stateIntegrationEntity = integrationEntity.getState();
		if (stateIntegrationEntity.getId() != IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC
				&& stateIntegrationEntity.getId() != IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT) {
			throw new BusinessException(
					"No se puede iniciar la integración asistida ya que no se encuentra en estado que no lo permite.");
		}

		// modify integration state

		String textHistory = userDto.getFirstName() + " " + userDto.getLastName() + " - " + managerDto.getName();
		integrationDto = integrationBusiness.updateStateToIntegration(integrationId,
				IntegrationStateBusiness.STATE_STARTED_ASSISTED, userDto.getId(), managerDto.getId(), textHistory);

		// create tasks

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
			listPropertiesConnection
					.add(new MicroserviceCreateTaskPropertyDto("host", integrationEntity.getHostname()));
			listPropertiesConnection.add(new MicroserviceCreateTaskPropertyDto("port", integrationEntity.getPort()));
			listPropertiesConnection
					.add(new MicroserviceCreateTaskPropertyDto("database", integrationEntity.getDatabase()));
			listPropertiesConnection
					.add(new MicroserviceCreateTaskPropertyDto("schema", integrationEntity.getSchema()));
			listPropertiesConnection
					.add(new MicroserviceCreateTaskPropertyDto("username", integrationEntity.getUsername()));
			listPropertiesConnection
					.add(new MicroserviceCreateTaskPropertyDto("password", integrationEntity.getPassword()));
			metadataConnection.setProperties(listPropertiesConnection);
			metadata.add(metadataConnection);

			MicroserviceCreateTaskMetadataDto metadataIntegration = new MicroserviceCreateTaskMetadataDto();
			metadataIntegration.setKey("integration");
			List<MicroserviceCreateTaskPropertyDto> listPropertiesIntegration = new ArrayList<>();
			listPropertiesIntegration
					.add(new MicroserviceCreateTaskPropertyDto("integration", integrationEntity.getId().toString()));

			List<MicroserviceCreateTaskStepDto> steps = new ArrayList<>();

			steps.add(new MicroserviceCreateTaskStepDto("Conectar a BD remota", "Conectar a BD remota",
					TaskBusiness.TASK_TYPE_STEP_ALWAYS));

			steps.add(new MicroserviceCreateTaskStepDto("Explorar datos de Catastro y Registro.",
					"Explorar datos de Catastro y Registro.", TaskBusiness.TASK_TYPE_STEP_ALWAYS));

			steps.add(new MicroserviceCreateTaskStepDto("Realizar integración asistida.",
					"Realizar integración asistida.", TaskBusiness.TASK_TYPE_STEP_ALWAYS));

			taskBusiness.createTask(taskCategories, sdf.format(cal.getTime()), description, name, users, metadata,
					steps);

		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
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

		if (managerDto.getId() != workspaceEntity.getManagerCode()) {
			throw new BusinessException("No tiene acceso al municipio.");
		}

		IntegrationEntity integrationEntity = integrationService.getIntegrationById(integrationId);
		if (!(integrationEntity instanceof IntegrationEntity)) {
			throw new BusinessException("No se ha encontrado la integración.");
		}

		if (integrationEntity.getWorkspace().getId() != workspaceEntity.getId()) {
			throw new BusinessException("La integración no pertenece al espacio de trabajo.");
		}

		IntegrationStateEntity stateEntity = integrationEntity.getState();
		if (stateEntity.getId() != IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC
				&& stateEntity.getId() != IntegrationStateBusiness.STATE_FINISHED_ASSISTED
				&& stateEntity.getId() != IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT) {
			throw new BusinessException(
					"No se puede generar el producto porque la integración se encuentra en un estado que no lo permite.");
		}

		try {

			MicroserviceIli2pgExportDto exportDto = new MicroserviceIli2pgExportDto();

			exportDto.setDatabaseHost(cryptoBusiness.decrypt(integrationEntity.getHostname()));
			exportDto.setDatabaseName(cryptoBusiness.decrypt(integrationEntity.getDatabase()));
			exportDto.setDatabasePassword(cryptoBusiness.decrypt(integrationEntity.getPassword()));
			exportDto.setDatabasePort(cryptoBusiness.decrypt(integrationEntity.getPort()));
			exportDto.setDatabaseSchema(cryptoBusiness.decrypt(integrationEntity.getSchema()));
			exportDto.setDatabaseUsername(cryptoBusiness.decrypt(integrationEntity.getUsername()));
			exportDto.setIntegrationId(integrationId);

			String randomFilename = RandomStringUtils.random(15, true, false).toLowerCase();
			exportDto.setPathFileXTF(stTemporalDirectory + File.separator + randomFilename + ".xtf");

			iliClient.startExport(exportDto);

			// modify integration state
			String textHistory = userDto.getFirstName() + " " + userDto.getLastName() + " - " + managerDto.getName();
			integrationDto = integrationBusiness.updateStateToIntegration(integrationId,
					IntegrationStateBusiness.STATE_GENERATING_PRODUCT, userDto.getId(), managerDto.getId(),
					textHistory);

		} catch (Exception e) {
			throw new BusinessException("No se ha podido iniciar la generación del insumo");
		}

		return integrationDto;
	}

}
