package com.ai.st.microservice.workspaces.business;

import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.OperatorFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.DepartmentDto;
import com.ai.st.microservice.workspaces.dto.MilestoneDto;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.StateDto;
import com.ai.st.microservice.workspaces.dto.SupportDto;
import com.ai.st.microservice.workspaces.dto.TypeSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceOperatorDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;

import com.ai.st.microservice.workspaces.dto.operators.OperatorDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceEmitterDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestEmitterDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyRequestedDto;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.MilestoneEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.StateEntity;
import com.ai.st.microservice.workspaces.entities.SupportEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceStateEntity;

import com.ai.st.microservice.workspaces.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.services.IMilestoneService;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IStateService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;
import com.ai.st.microservice.workspaces.services.RabbitMQSenderService;

import feign.FeignException;

@Component
public class WorkspaceBusiness {

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
	private IMunicipalityService municipalityService;

	@Autowired
	private IWorkspaceService workspaceService;

	@Autowired
	private IMilestoneService milestoneService;

	@Autowired
	private IStateService stateService;

	@Autowired
	private RabbitMQSenderService rabbitMQSender;

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

		// save file with microservice filemanager
		String urlDocumentaryRepository = null;
		try {

			String urlBase = "/" + municipalityEntity.getCode() + "/soportes/gestores";

			urlDocumentaryRepository = rabbitMQSender.sendFile(supportFile.getBytes(),
					StringUtils.cleanPath(supportFile.getOriginalFilename()), urlBase, "Local");

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
		} catch (FeignException e) {
			throw new BusinessException("No se ha encontrado el operador.");
		}

		// validate that the workspace does not already have an operator assigned
		if (workspaceEntity.getOperators().size() > 0) {

			// generate new workspace
			workspaceEntity = cloneWorkspace(workspaceId, WorkspaceBusiness.WORKSPACE_CLONE_FROM_CHANGE_OPERATOR);
		}

		// save file with microservice filemanager
		String urlDocumentaryRepository = null;
		try {

			String urlBase = "/" + workspaceEntity.getMunicipality().getCode() + "/soportes/operadores";

			urlDocumentaryRepository = rabbitMQSender.sendFile(supportFile.getBytes(),
					StringUtils.cleanPath(supportFile.getOriginalFilename()), urlBase, "Local");

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

}
