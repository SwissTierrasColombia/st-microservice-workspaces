package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceExtensionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyOwnerDto;
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

	public List<MicroserviceSupplyDto> getSuppliesByMunicipalityAdmin(Long municipalityId, List<String> extensions)
			throws BusinessException {

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("No se ha encontrado el municipio.");
		}

		return this.getSuppliesByMunicipality(municipalityEntity.getCode(), extensions);
	}

	public List<MicroserviceSupplyDto> getSuppliesByMunicipalityManager(Long municipalityId, Long managerCode,
			List<String> extensions) throws BusinessException {

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

		return this.getSuppliesByMunicipality(municipalityEntity.getCode(), extensions);
	}

	private List<MicroserviceSupplyDto> getSuppliesByMunicipality(String municipalityCode, List<String> extensions)
			throws BusinessException {

		List<MicroserviceSupplyDto> suppliesDto = new ArrayList<>();

		try {
			suppliesDto = supplyClient.getSuppliesByMunicipalityCode(municipalityCode);

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

		List<MicroserviceSupplyDto> suppliesFinal = new ArrayList<>();

		if (extensions != null && extensions.size() > 0) {

			for (MicroserviceSupplyDto supplyDto : suppliesDto) {
				List<MicroserviceExtensionDto> extensionsDto = supplyDto.getTypeSupply().getExtensions();
				for (MicroserviceExtensionDto extensionDto : extensionsDto) {

					String extensionFound = extensions.stream()
							.filter(extension -> extensionDto.getName().toLowerCase().equals(extension.toLowerCase()))
							.findAny().orElse(null);
					if (extensionFound != null) {
						suppliesFinal.add(supplyDto);
					}
				}
			}

		} else {
			suppliesFinal = suppliesDto;
		}

		return suppliesFinal;
	}

	public MicroserviceSupplyDto createSupply(String municipalityCode, String observations, Long typeSupplyCode,
			List<String> urlsAttachments, String url, Long userCode, Long providerCode, Long managerCode)
			throws BusinessException {

		MicroserviceSupplyDto supplyDto = null;

		try {

			MicroserviceCreateSupplyDto createSupplyDto = new MicroserviceCreateSupplyDto();
			createSupplyDto.setMunicipalityCode(municipalityCode);
			createSupplyDto.setObservations(observations);

			if (typeSupplyCode != null) {
				createSupplyDto.setTypeSupplyCode(typeSupplyCode);
			}

			createSupplyDto.setUrlsDocumentaryRepository(urlsAttachments);
			if (url != null && !url.isEmpty()) {
				createSupplyDto.setUrl(url);
			}

			List<MicroserviceCreateSupplyOwnerDto> owners = new ArrayList<MicroserviceCreateSupplyOwnerDto>();

			if (userCode != null) {
				MicroserviceCreateSupplyOwnerDto owner = new MicroserviceCreateSupplyOwnerDto();
				owner.setOwnerCode(userCode);
				owner.setOwnerType("USER");
				owners.add(owner);
			}

			if (providerCode != null) {
				MicroserviceCreateSupplyOwnerDto owner = new MicroserviceCreateSupplyOwnerDto();
				owner.setOwnerCode(providerCode);
				owner.setOwnerType("ENTITY_PROVIDER");
				owners.add(owner);
			}

			if (managerCode != null) {
				MicroserviceCreateSupplyOwnerDto owner = new MicroserviceCreateSupplyOwnerDto();
				owner.setOwnerCode(managerCode);
				owner.setOwnerType("ENTITY_MANAGER");
				owners.add(owner);
			}

			createSupplyDto.setOwners(owners);

			supplyDto = supplyClient.createSupply(createSupplyDto);

		} catch (Exception e) {
			throw new BusinessException("No se ha podido cargar el insumo");
		}

		return supplyDto;
	}

}
