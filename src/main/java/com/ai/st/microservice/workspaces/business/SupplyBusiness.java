package com.ai.st.microservice.workspaces.business;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.SupplyFeignClient;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceExtensionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceDataPaginatedDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyAttachmentDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyAttachmentDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyOwnerDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceUpdateSupplyDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IWorkspaceService;
import com.ai.st.microservice.workspaces.utils.DateTool;
import com.ai.st.microservice.workspaces.utils.FileTool;

@Component
public class SupplyBusiness {

	@Value("${st.temporalDirectory}")
	private String stTemporalDirectory;

	private final Logger log = LoggerFactory.getLogger(SupplyBusiness.class);

	// attachments types
	public static final Long SUPPLY_ATTACHMENT_TYPE_SUPPLY = (long) 1;
	public static final Long SUPPLY_ATTACHMENT_TYPE_FTP = (long) 2;
	public static final Long SUPPLY_ATTACHMENT_TYPE_EXTERNAL_SOURCE = (long) 3;

	// states
	public static final Long SUPPLY_STATE_ACTIVE = (long) 1;
	public static final Long SUPPLY_STATE_INACTIVE = (long) 2;
	public static final Long SUPPLY_STATE_REMOVED = (long) 3;

	@Autowired
	private IMunicipalityService municipalityService;

	@Autowired
	private IWorkspaceService workspaceService;

	@Autowired
	private SupplyFeignClient supplyClient;

	@Autowired
	private ProviderFeignClient providerClient;

	@Autowired
	private OperatorBusiness operatorBusiness;

	public Object getSuppliesByMunicipalityAdmin(Long municipalityId, List<String> extensions, Integer page,
			List<Long> requests, boolean active) throws BusinessException {

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("No se ha encontrado el municipio.");
		}

		return this.getSuppliesByMunicipality(municipalityEntity, extensions, page, requests, active);
	}

	public Object getSuppliesByMunicipalityManager(Long municipalityId, Long managerCode, List<String> extensions,
			Integer page, List<Long> requests, boolean active) throws BusinessException {

		// validate if the municipality exists
		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);
		if (!(municipalityEntity instanceof MunicipalityEntity)) {
			throw new BusinessException("No se ha encontrado el municipio.");
		}

		if (managerCode != null) {
			WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
			if (workspaceActive instanceof WorkspaceEntity) {

				/**
				 * TODO: Refactoring pending ...
				 * 
				 * Before:
				 * 
				 * if (!managerCode.equals(workspaceActive.getManagerCode())) { throw new
				 * BusinessException("No tiene acceso al municipio."); }
				 * 
				 * 
				 */

				if (!managerCode.equals(null)) {
					throw new BusinessException("No tiene acceso al municipio.");
				}
			}
		}

		return this.getSuppliesByMunicipality(municipalityEntity, extensions, page, requests, active);
	}

	private Object getSuppliesByMunicipality(MunicipalityEntity municipality, List<String> extensions, Integer page,
			List<Long> requests, boolean active) throws BusinessException {

		List<Long> states = new ArrayList<>();

		if (active) {
			states.add(SupplyBusiness.SUPPLY_STATE_ACTIVE);
		} else {
			states.add(SupplyBusiness.SUPPLY_STATE_ACTIVE);
			states.add(SupplyBusiness.SUPPLY_STATE_INACTIVE);
			states.add(SupplyBusiness.SUPPLY_STATE_REMOVED);
		}

		List<MicroserviceSupplyDto> suppliesDto = new ArrayList<>();

		try {

			MicroserviceDataPaginatedDto dataPaginated = null;

			if (page != null) {
				dataPaginated = supplyClient.getSuppliesByMunicipalityCodeByFilters(municipality.getCode(), page,
						requests, states);
				suppliesDto = dataPaginated.getItems();
			} else {
				suppliesDto = supplyClient.getSuppliesByMunicipalityCode(municipality.getCode(), states);
			}

			for (MicroserviceSupplyDto supplyDto : suppliesDto) {

				if (supplyDto.getTypeSupplyCode() != null) {

					try {
						MicroserviceTypeSupplyDto typeSupplyDto = providerClient
								.findTypeSuppleById(supplyDto.getTypeSupplyCode());

						supplyDto.setTypeSupply(typeSupplyDto);

					} catch (Exception e) {
						throw new BusinessException("No se ha podido consultar el tipo de insumo.");
					}

				}

				// verify if the supply has been delivered to operator
				try {

					WorkspaceEntity workspaceActive = workspaceService.getWorkspaceActiveByMunicipality(municipality);

					if (workspaceActive != null) {
						List<WorkspaceOperatorEntity> operators = workspaceActive.getOperators();

						if (operators.size() >= 1) {

							List<MicroserviceDeliveryDto> deliveriesDto = operatorBusiness.getDeliveriesByOperator(
									operators.get(0).getOperatorCode(), municipality.getCode());

							for (MicroserviceDeliveryDto deliveryFoundDto : deliveriesDto) {

								MicroserviceSupplyDeliveryDto supplyFound = deliveryFoundDto.getSupplies().stream()
										.filter(sDto -> sDto.getSupplyCode().equals(supplyDto.getId())).findAny()
										.orElse(null);

								if (supplyFound != null) {
									supplyDto.setDelivered(true);
									supplyDto.setDelivery(deliveryFoundDto);
								}
							}

						}
					}

				} catch (Exception e) {
					log.error(
							"No se ha podido consultar si el insumo ha sido entregado al operador: " + e.getMessage());
				}

			}

			if (page != null) {
				return dataPaginated;
			}

		} catch (Exception e) {
			throw new BusinessException("No se ha podido consultar los insumos del municipio.");
		}

		List<MicroserviceSupplyDto> suppliesFinal = new ArrayList<>();

		if (extensions != null && extensions.size() > 0) {

			for (MicroserviceSupplyDto supplyDto : suppliesDto) {

				if (supplyDto.getTypeSupply() != null) {
					List<MicroserviceExtensionDto> extensionsDto = supplyDto.getTypeSupply().getExtensions();
					for (MicroserviceExtensionDto extensionDto : extensionsDto) {

						String extensionFound = extensions.stream().filter(
								extension -> extensionDto.getName().toLowerCase().equals(extension.toLowerCase()))
								.findAny().orElse(null);
						if (extensionFound != null) {
							suppliesFinal.add(supplyDto);
						}
					}
				}
			}

		} else {
			suppliesFinal = suppliesDto;
		}

		return suppliesFinal;
	}

	public MicroserviceSupplyDto createSupply(String municipalityCode, String observations, Long typeSupplyCode,
			List<MicroserviceCreateSupplyAttachmentDto> attachments, Long requestId, Long userCode, Long providerCode,
			Long managerCode, Long cadastralAuthority, String modelVersion, Long stateSupplyId, String name)
			throws BusinessException {

		MicroserviceSupplyDto supplyDto = null;

		try {

			MicroserviceCreateSupplyDto createSupplyDto = new MicroserviceCreateSupplyDto();
			createSupplyDto.setMunicipalityCode(municipalityCode);
			createSupplyDto.setObservations(observations);
			createSupplyDto.setModelVersion(modelVersion);

			if (stateSupplyId != null) {
				createSupplyDto.setSupplyStateId(stateSupplyId);
			}

			if (name != null) {
				createSupplyDto.setName(name);
			}

			if (requestId != null) {
				createSupplyDto.setRequestCode(requestId);
			}

			if (typeSupplyCode != null) {
				createSupplyDto.setTypeSupplyCode(typeSupplyCode);
			}

			createSupplyDto.setAttachments(attachments);

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

			if (cadastralAuthority != null) {
				MicroserviceCreateSupplyOwnerDto owner = new MicroserviceCreateSupplyOwnerDto();
				owner.setOwnerCode(cadastralAuthority);
				owner.setOwnerType("CADASTRAL_AUTHORITY");
				owners.add(owner);
			}

			createSupplyDto.setOwners(owners);

			supplyDto = supplyClient.createSupply(createSupplyDto);

		} catch (Exception e) {
			log.error("No se ha podido crear el insumo: " + e.getMessage());
			throw new BusinessException("No se ha podido cargar el insumo");
		}

		return supplyDto;
	}

	public MicroserviceSupplyDto getSupplyById(Long supplyId) throws BusinessException {

		MicroserviceSupplyDto supplyDto = null;

		try {
			supplyDto = supplyClient.findSupplyById(supplyId);

			if (supplyDto.getTypeSupplyCode() != null) {

				MicroserviceTypeSupplyDto typeSupplyDto = providerClient
						.findTypeSuppleById(supplyDto.getTypeSupplyCode());

				supplyDto.setTypeSupply(typeSupplyDto);
			}

		} catch (Exception e) {
			log.error("No se ha podido consultar el insumo: " + e.getMessage());
		}

		return supplyDto;
	}

	public void deleteSupply(Long supplyId) throws BusinessException {

		try {

			supplyClient.deleteSupplyById(supplyId);

		} catch (Exception e) {
			log.error("No se ha podido eliminar el insumo: " + e.getMessage());
		}

	}

	public File generateFTPFile(MicroserviceSupplyDto supplyDto, MunicipalityDto municipalityDto) {

		String randomCode = RandomStringUtils.random(10, false, true);

		String filename = stTemporalDirectory + File.separatorChar + "insumo_" + randomCode + ".txt";

		MicroserviceSupplyAttachmentDto attachmentFtp = supplyDto.getAttachments().stream()
				.filter(a -> a.getAttachmentType().getId().equals(SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_FTP)).findAny()
				.orElse(null);

		String content = "***********************************************" + "\n";
		content += "Sistema de transición Barrido Predial \n";
		content += "Fecha de Cargue del Insumo: " + DateTool.formatDate(supplyDto.getCreatedAt(), "yyyy-MM-dd") + "\n";
		content += "***********************************************" + "\n";
		content += "Código de Municipio: " + municipalityDto.getCode() + "\n";
		content += "Municipio: " + municipalityDto.getName() + "\n";
		content += "Departamento: " + municipalityDto.getDepartment().getName() + "\n";
		content += "***********************************************" + "\n";

		if (supplyDto.getTypeSupply() != null) {
			String typeSupplyName = supplyDto.getTypeSupply().getName().replace(" ", "_");
			content += "Nombre Insumo: " + typeSupplyName + "\n";
			content += "Proveedor: " + supplyDto.getTypeSupply().getProvider().getName() + "\n";
			content += "***********************************************" + "\n";
		}

		content += "URL: " + attachmentFtp.getData() + "\n";
		content += "Observaciones: " + supplyDto.getObservations() + "\n";
		content += "***********************************************" + "\n";

		File fileSupply = FileTool.createSimpleFile(content, filename);

		return fileSupply;
	}

	public MicroserviceSupplyDto changeStateSupply(Long supplyId, Long stateId, Long managerCode)
			throws BusinessException {

		List<WorkspaceEntity> workspaces = workspaceService.getWorkspacesByManagerAndIsActive(managerCode, true);

		MicroserviceSupplyDto supplyDto = getSupplyById(supplyId);
		if (supplyDto == null) {
			throw new BusinessException("No se ha encontrado el insumo");
		}

		boolean belong = false;
		for (WorkspaceEntity workspace : workspaces) {
			if (workspace.getMunicipality().getCode().equals(supplyDto.getMunicipalityCode())) {
				belong = true;
				break;
			}
		}

		if (!belong) {
			throw new BusinessException("El gestor no tiene permisos para modificar el insumo");
		}

		try {

			MicroserviceUpdateSupplyDto data = new MicroserviceUpdateSupplyDto();
			data.setStateId(stateId);

			supplyDto = supplyClient.updateSupply(supplyId, data);

		} catch (BusinessException e) {
			log.error("Error actualizando el estado del insumo: " + e.getMessage());
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			log.error("Error actualizando el estado del insumo: " + e.getMessage());
			throw new BusinessException("No se ha podido cambiar el estado del insumo.");
		}

		return supplyDto;
	}

}
