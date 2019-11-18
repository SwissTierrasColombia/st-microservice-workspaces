package com.ai.st.microservice.workspaces.business;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.dto.ManagerDto;
import com.ai.st.microservice.workspaces.dto.WorkspaceDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;

import feign.FeignException;

@Component
public class WorkspaceBusiness {

	@Autowired
	private ManagerFeignClient managerClient;

	@Autowired
	private IMunicipalityService municipalityService;;

	public WorkspaceDto createWorkspace(Date startDate, Date endDate, Long managerCode, Long municipalityId,
			String observations, Long parcelsNumber) throws BusinessException {

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

		return workspaceDto;
	}

}
