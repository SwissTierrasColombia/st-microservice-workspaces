package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceReportInformationDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceSupplyACDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyAttachmentDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyOwnerDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.services.MunicipalityService;
import com.ai.st.microservice.workspaces.services.WorkspaceService;
import com.ai.st.microservice.workspaces.utils.DateTool;
import com.ai.st.microservice.workspaces.utils.FileTool;

@Component
public class CadastralAuthorityBusiness {

	@Value("${st.filesDirectory}")
	private String stFilesDirectory;

	@Autowired
	private MunicipalityBusiness municipalityBusiness;

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private FileBusiness fileBusiness;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private ReportBusiness reportBusiness;

	@Autowired
	private ManagerBusiness managerBusiness;

	public MicroserviceSupplyDto createSupplyCadastralAuthority(Long municipalityId, Long attachmentTypeId, String name,
			String observations, String ftp, MultipartFile file, Long userCode) throws BusinessException {

		MicroserviceSupplyDto supplyDto = null;

		if (ftp == null && file == null) {
			throw new BusinessException("Se debe cargar algun tipo de adjunto");
		}

		MunicipalityDto municipalityDto = municipalityBusiness.getMunicipalityById(municipalityId);
		if (municipalityDto == null) {
			throw new BusinessException("El municipio no existe");
		}

		String municipalityCode = municipalityDto.getCode();

		List<MicroserviceCreateSupplyAttachmentDto> attachments = new ArrayList<>();

		if (file != null) {

			if (attachmentTypeId != SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_EXTERNAL_SOURCE
					&& attachmentTypeId != SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY) {
				throw new BusinessException("No se puede cargar un archivo para el tipo de insumo seleccionado.");
			}

			String loadedFileName = file.getOriginalFilename();
			String loadedFileExtension = FilenameUtils.getExtension(loadedFileName);

			Boolean zipFile = true;
			if (loadedFileExtension.equalsIgnoreCase("zip")) {
				zipFile = false;
			}

			// save file
			String urlBase = "/" + municipalityCode.replace(" ", "_") + "/insumos/autoridad_catastral";
			urlBase = FileTool.removeAccents(urlBase);
			String urlDocumentaryRepository = fileBusiness.saveFileToSystem(file, urlBase, zipFile);

			attachments.add(new MicroserviceCreateSupplyAttachmentDto(urlDocumentaryRepository, attachmentTypeId));

		} else if (ftp != null) {

			if (attachmentTypeId != SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_FTP) {
				throw new BusinessException("No se puede cargar FTP para el tipo de insumo seleccionado.");
			}

			attachments.add(new MicroserviceCreateSupplyAttachmentDto(ftp, SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_FTP));
		}

		try {
			supplyDto = supplyBusiness.createSupply(municipalityCode, observations, null, attachments, null, userCode,
					null, null, userCode, null, SupplyBusiness.SUPPLY_STATE_INACTIVE, name);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido cargar el insumo.");
		}

		return supplyDto;
	}

	public String generateReport(Long municipalityId) throws BusinessException {

		MunicipalityDto municipalityDto = municipalityBusiness.getMunicipalityById(municipalityId);
		if (municipalityDto == null) {
			throw new BusinessException("El municipio no existe");
		}

		MunicipalityEntity municipalityEntity = municipalityService.getMunicipalityById(municipalityId);

		WorkspaceEntity workspaceEntity = workspaceService.getWorkspaceActiveByMunicipality(municipalityEntity);
		if (workspaceEntity == null) {
			throw new BusinessException("El municipio aún no tiene asignado un gestor");
		}

		/**
		 * TODO: Refactoring pending ...
		 * 
		 * Before:
		 * 
		 * MicroserviceManagerDto managerDto =
		 * managerBusiness.getManagerById(workspaceEntity.getManagerCode());
		 * 
		 * 
		 */

		MicroserviceManagerDto managerDto = managerBusiness.getManagerById(null);
		if (!(managerDto instanceof MicroserviceManagerDto)) {
			throw new BusinessException("No se ha encontrado el gestor.");
		}

		String format = "yyyy-MM-dd hh:mm:ss";

		// configuration params
		String namespace = "/" + municipalityDto.getCode() + "/reportes/entregas/ac/";
		String createdAt = DateTool.formatDate(new Date(), format);
		String departmentName = municipalityDto.getDepartment().getName();
		String managerName = managerDto.getName();
		String municipalityCode = municipalityDto.getCode();
		String municipalityName = municipalityDto.getName();

		List<MicroserviceSupplyACDto> suppliesReport = new ArrayList<MicroserviceSupplyACDto>();

		@SuppressWarnings("unchecked")
		List<MicroserviceSupplyDto> supplies = (List<MicroserviceSupplyDto>) supplyBusiness
				.getSuppliesByMunicipalityAdmin(municipalityId, new ArrayList<>(), null, null, false);

		for (MicroserviceSupplyDto supply : supplies) {

			MicroserviceSupplyOwnerDto owner = supply.getOwners().stream()
					.filter(o -> o.getOwnerType().equalsIgnoreCase("CADASTRAL_AUTHORITY")).findAny().orElse(null);
			if (owner != null) {

				String supplyName = supply.getName();
				String providerName = "Autoridad Catastral";

				String type = supply.getAttachments().get(0).getAttachmentType().getName().toUpperCase();

				suppliesReport.add(new MicroserviceSupplyACDto(supplyName,
						DateTool.formatDate(supply.getCreatedAt(), format), type, providerName));

			}

		}

		if (suppliesReport.size() == 0) {
			throw new BusinessException("No se puede generar el reporte porque no hay insumos entregados.");
		}

		MicroserviceReportInformationDto report = reportBusiness.generateReportDeliveryAC(namespace, createdAt,
				departmentName, managerName, municipalityCode, municipalityName, suppliesReport);

		return report.getUrlReport();
	}

}
