package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.clients.FilemanagerFeignClient;
import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.dto.ManagerDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.entities.MilestoneEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.SupportEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IMilestoneService;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;

import feign.FeignException;

@Component
public class WorkspaceBusiness {

	@Autowired
	private ManagerFeignClient managerClient;

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
			throw new BusinessException("Manager not found.");
		}

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("Municipality not found.");
		}

		// save file
//		try {
//			filemanagerClient.saveFile(supportFile, "Local");
//		} catch (FeignException e) {
//			System.out.println("errorrrr " + e.getMessage());
//			throw new BusinessException("No se ha podido guardar el archivo.");
//		}

		MilestoneEntity milestoneNewWorkspace = milestoneService
				.getMilestoneById(MilestoneBusiness.MILESTONE_NEW_WORKSPACE);

		Long countWorkspaces = workspaceService.getCountByMunicipality(municipalityEntity);

		WorkspaceEntity workspaceEntity = new WorkspaceEntity();
		workspaceEntity.setCreatedAt(new Date());
		workspaceEntity.setIsActive(true);
		workspaceEntity.setManagerCode(managerCode);
		workspaceEntity.setObservations(observations);
		workspaceEntity.setNumberAlphanumericParcels(parcelsNumber);
		workspaceEntity.setMunicipalityArea(municipalityArea);
		workspaceEntity.setStartDate(startDate);
		workspaceEntity.setVersion(countWorkspaces + 1);
		workspaceEntity.setMunicipality(municipalityEntity);

		// support
		SupportEntity supporEntity = new SupportEntity();
		supporEntity.setCreatedAt(new Date());
		supporEntity.setUrlDocumentaryRepository("test");
		supporEntity.setWorkspace(workspaceEntity);
		supporEntity.setMilestone(milestoneNewWorkspace);

		List<SupportEntity> supports = new ArrayList<SupportEntity>();
		supports.add(supporEntity);
		workspaceEntity.setSupports(supports);

		workspaceEntity = workspaceService.createWorkspace(workspaceEntity);

		workspaceDto = new WorkspaceDto();
		workspaceDto.setId(workspaceEntity.getId());
		workspaceDto.setCreatedAt(workspaceEntity.getCreatedAt());
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

}
