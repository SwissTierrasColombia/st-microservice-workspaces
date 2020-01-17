package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;

@Component
public class SupplyBusiness {

	@Autowired
	private IMunicipalityService municipalityService;

	@Autowired
	private IWorkspaceService workspaceService;

	@Autowired
	private SupplyFeignClient supplyClient;

	@Autowired
	private ProviderFeignClient providerClient;

	public List<MicroserviceSupplyDto> getSuppliesByMunicipalityAdmin(Long municipalityId) throws BusinessException {

		List<MicroserviceSupplyDto> suppliesDto = new ArrayList<>();

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("No se ha encontrado el municipio.");
		}

		try {
			suppliesDto = supplyClient.getSuppliesByMunicipalityCode(municipalityEntity.getCode());

			for (MicroserviceSupplyDto supplyDto : suppliesDto) {

				try {
					MicroserviceTypeSupplyDto typeSupplyDto = providerClient
							.findTypeSuppleById(supplyDto.getTypeSupplyCode());

					supplyDto.setTypeSupply(typeSupplyDto);

				} catch (Exception e) {
					throw new BusinessException("No se ha podido consultar el tipo de insumo.");
				}

			}

		} catch (Exception e) {
			throw new BusinessException("No se ha podido consultar los insumos del municipio.");
		}

		return suppliesDto;
	}

	public List<MicroserviceSupplyDto> getSuppliesByMunicipalityManager(Long municipalityId, Long managerCode)
			throws BusinessException {

		List<MicroserviceSupplyDto> suppliesDto = new ArrayList<>();

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("No se ha encontrado el municipio.");
		}

		if (managerCode != null) {
			WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
			if (workspaceActive instanceof WorkspaceEntity) {
				if (managerCode != workspaceActive.getManagerCode()) {
					throw new BusinessException("No tiene acceso al municipio.");
				}
			}
		}

		try {
			suppliesDto = supplyClient.getSuppliesByMunicipalityCode(municipalityEntity.getCode());
		} catch (Exception e) {
			throw new BusinessException("No se ha podido consultar los insumos del municipio.");
		}

		return suppliesDto;
	}

}
