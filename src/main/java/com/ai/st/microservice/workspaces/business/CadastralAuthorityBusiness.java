package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyAttachmentDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.utils.FileTool;

@Component
public class CadastralAuthorityBusiness {

	@Value("${st.filesDirectory}")
	private String stFilesDirectory;

	@Autowired
	private MunicipalityBusiness municipalityBusiness;

	@Autowired
	private FileBusiness fileBusiness;

	@Autowired
	private SupplyBusiness supplyBusiness;

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

}
