package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.FilemanagerFeignClient;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.OperatorFeignClient;
import com.ai.st.microservice.workspaces.dto.ManagerDto;
import com.ai.st.microservice.workspaces.dto.MilestoneDto;
import com.ai.st.microservice.workspaces.dto.OperatorDto;
import com.ai.st.microservice.workspaces.dto.SupportDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceOperatorDto;
import com.ai.st.microservice.workspaces.entities.MilestoneEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.SupportEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IMilestoneService;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;

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
	private FilemanagerFeignClient filemanagerClient;

	@Autowired
	private IMunicipalityService municipalityService;

	@Autowired
	private IWorkspaceService workspaceService;

	@Autowired
	private IMilestoneService milestoneService;

	public WorkspaceDto createWorkspace(Date startDate, Long managerCode, Long municipalityId, String observations,
			Long parcelsNumber, Double municipalityArea, MultipartFile supportFile) throws BusinessException {

		WorkspaceDto workspaceDto = null;

		// validate if the manager exists
		ManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(managerCode);
		} catch (FeignException e) {
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

		// TODO: save file with microservice filemanager

		MilestoneEntity milestoneNewWorkspace = milestoneService
				.getMilestoneById(MilestoneBusiness.MILESTONE_NEW_WORKSPACE);

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

		// support
		SupportEntity supporEntity = new SupportEntity();
		supporEntity.setCreatedAt(new Date());
		supporEntity.setUrlDocumentaryRepository("test");
		supporEntity.setWorkspace(workspaceEntity);
		supporEntity.setMilestone(milestoneNewWorkspace);

		List<SupportEntity> supports = workspaceEntity.getSupports();
		supports.add(supporEntity);
		workspaceEntity.setSupports(supports);

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

			// get the manager
			try {
				ManagerDto managerDto = managerClient.findById(workspaceEntity.getManagerCode());
				workspaceDto.setManager(managerDto);
			} catch (FeignException e) {
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
			System.out.println("clonado " + workspaceEntity.getId());
		}

		// TODO: save support file with microservice filemanager

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
		supporEntity.setUrlDocumentaryRepository("test");
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

		ManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(workspaceEntity.getManagerCode());
			workspaceDto.setManager(managerDto);
		} catch (FeignException e) {
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

		ManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(workspaceEntity.getManagerCode());
			workspaceDto.setManager(managerDto);
		} catch (FeignException e) {
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
			cloneWorkspaceEntity.setWorkspace(workspaceEntityFound);

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

		ManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(workspaceEntity.getManagerCode());
			workspaceDto.setManager(managerDto);
		} catch (FeignException e) {
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
			} catch (FeignException e) {
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

		ManagerDto managerDto = null;
		try {
			managerDto = managerClient.findById(workspaceEntity.getManagerCode());
			workspaceDto.setManager(managerDto);
		} catch (FeignException e) {
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

}
