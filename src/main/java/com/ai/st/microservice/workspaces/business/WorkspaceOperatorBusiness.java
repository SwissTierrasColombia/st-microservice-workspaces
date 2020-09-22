package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceDownloadedSupplyDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceReportInformationDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.utils.DateTool;

@Component
public class WorkspaceOperatorBusiness {

	private final Logger log = LoggerFactory.getLogger(WorkspaceOperatorBusiness.class);

	@Autowired
	private OperatorBusiness operatorBusiness;

	@Autowired
	private ReportBusiness reportBusiness;

	@Autowired
	private MunicipalityBusiness municipalityBusiness;

	@Autowired
	private ManagerBusiness managerBusiness;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private UserBusiness userBusiness;

	public MicroserviceDeliveryDto getDeliveryFromSupply(Long operatorCode, Long supplyCode) throws BusinessException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {

			List<MicroserviceDeliveryDto> deliveries = operatorBusiness.getDeliveriesActivesByOperator(operatorCode);

			for (MicroserviceDeliveryDto delivery : deliveries) {

				MicroserviceSupplyDeliveryDto supplyDto = delivery.getSupplies().stream()
						.filter(s -> s.getSupplyCode().equals(supplyCode)).findAny().orElse(null);

				if (supplyDto != null) {
					return delivery;
				}

			}

		} catch (Exception e) {
			log.error("Error consultando la entrega por insumo: " + e.getMessage());
			throw new BusinessException("No se ha podido obtener la entrega correspondiente al insumo.");
		}

		return deliveryDto;
	}

	public MicroserviceDeliveryDto registerDownloadSupply(MicroserviceDeliveryDto deliveryDto, Long supplyCode,
			Long userCode) {

		try {

			MicroserviceSupplyDeliveryDto supplyDto = deliveryDto.getSupplies().stream()
					.filter(s -> s.getSupplyCode().equals(supplyCode)).findAny().orElse(null);

			if (supplyDto != null && !supplyDto.getDownloaded()) {
				deliveryDto = operatorBusiness.updateSupplyDeliveredDownloaded(deliveryDto.getId(), supplyCode,
						userCode);
			}

		} catch (Exception e) {
			log.error("Error consultando la entrega por insumo: " + e.getMessage());
		}

		return deliveryDto;
	}

	public MicroserviceDeliveryDto disableDelivery(Long operatorId, Long deliveryId) throws BusinessException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {
			deliveryDto = operatorBusiness.getDeliveryId(deliveryId);
		} catch (Exception e) {
			log.error("Error consultando entrega por id: " + e.getMessage());
		}

		if (deliveryDto == null) {
			throw new BusinessException("No se ha encontrado la entrega.");
		}

		if (!deliveryDto.getOperator().getId().equals(operatorId)) {
			throw new BusinessException("La entrega no pertenece al operador.");
		}

		try {
			deliveryDto = operatorBusiness.disableDelivery(deliveryId);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido desactivar la entrega.");
		}

		return deliveryDto;
	}

	public String generateReportDownloadSupplyIndividual(Long operatorId, Long deliveryId, Long supplyId)
			throws BusinessException {

		MicroserviceDeliveryDto deliveryDto = null;
		MicroserviceOperatorDto operatorDto = null;

		try {
			deliveryDto = operatorBusiness.getDeliveryId(deliveryId);
		} catch (Exception e) {
			log.error("Error consultando entrega por id: " + e.getMessage());
		}

		try {
			operatorDto = operatorBusiness.getOperatorById(operatorId);
		} catch (Exception e) {
			log.error("Error consultando operador por id: " + e.getMessage());
		}

		if (operatorDto == null) {
			throw new BusinessException("No se ha encontrado el operador.");
		}

		if (deliveryDto == null) {
			throw new BusinessException("No se ha encontrado la entrega.");
		}

		if (!deliveryDto.getOperator().getId().equals(operatorId)) {
			throw new BusinessException("La entrega no pertenece al operador.");
		}

		if (!deliveryDto.getIsActive()) {
			throw new BusinessException("La entrega no se encuentra activa para generar el reporte solicitado.");
		}

		MicroserviceSupplyDeliveryDto supplyDeliveryDto = deliveryDto.getSupplies().stream()
				.filter(s -> s.getSupplyCode().equals(supplyId)).findAny().orElse(null);
		if (supplyDeliveryDto == null) {
			throw new BusinessException("El insumo no pertenece a la entrega.");
		}

		if (!supplyDeliveryDto.getDownloaded()) {
			throw new BusinessException("No se puede generar el reporte porque aún no se ha descargado el reporte.");
		}

		if (supplyDeliveryDto.getDownloadReportUrl() != null && !supplyDeliveryDto.getDownloadReportUrl().isEmpty()) {
			return supplyDeliveryDto.getDownloadReportUrl();
		}

		MunicipalityDto municipalityDto = municipalityBusiness.getMunicipalityByCode(deliveryDto.getMunicipalityCode());
		if (!(municipalityDto instanceof MunicipalityDto)) {
			throw new BusinessException("No se ha encontrado el municipio.");
		}

		MicroserviceManagerDto managerDto = managerBusiness.getManagerById(deliveryDto.getManagerCode());
		if (!(managerDto instanceof MicroserviceManagerDto)) {
			throw new BusinessException("No se ha encontrado el gestor.");
		}

		String format = "yyyy-MM-dd hh:mm:ss";

		// configuration parameters
		String namespace = "/" + deliveryDto.getMunicipalityCode() + "/reportes/entregas/" + deliveryDto.getId() + "/";
		String dateCreation = DateTool.formatDate(new Date(), format);
		String dateDelivery = DateTool.formatDate(deliveryDto.getCreatedAt(), format);
		String departmentName = municipalityDto.getDepartment().getName();
		String managerName = managerDto.getName();
		String municipalityCode = municipalityDto.getCode();
		String municipalityName = municipalityDto.getName();
		String observations = deliveryDto.getObservations();
		String operatorName = operatorDto.getName();

		List<MicroserviceDownloadedSupplyDto> supplies = new ArrayList<MicroserviceDownloadedSupplyDto>();
		String supplyName = "";
		String providerName = "";
		MicroserviceSupplyDto supplyDto = supplyBusiness.getSupplyById(supplyDeliveryDto.getSupplyCode());
		if (supplyDto != null) {
			supplyName = (supplyDto.getTypeSupply() != null) ? supplyDto.getTypeSupply().getName() : "";
			providerName = (supplyDto.getTypeSupply() != null) ? supplyDto.getTypeSupply().getProvider().getName() : "";
		}

		String downloadedBy = "";
		MicroserviceUserDto userDto = userBusiness.getUserById(supplyDeliveryDto.getDownloadedBy());
		if (userDto != null) {
			downloadedBy = userDto.getFirstName() + " " + userDto.getLastName();
		}

		supplies.add(new MicroserviceDownloadedSupplyDto(supplyName,
				DateTool.formatDate(supplyDeliveryDto.getDownloadedAt(), format), downloadedBy, providerName));

		MicroserviceReportInformationDto report = reportBusiness.generateReportDownloadSupply(namespace, dateCreation,
				dateDelivery, deliveryId.toString(), departmentName, managerName, municipalityCode, municipalityName,
				observations, operatorName, supplies);

		// update URL report
		operatorBusiness.updateSupplyDeliveredReportURL(deliveryId, supplyId, report.getUrlReport());

		return report.getUrlReport();
	}

	public String generateReportDownloadSupplyTotal(Long operatorId, Long deliveryId) throws BusinessException {

		MicroserviceDeliveryDto deliveryDto = null;
		MicroserviceOperatorDto operatorDto = null;

		try {
			deliveryDto = operatorBusiness.getDeliveryId(deliveryId);
		} catch (Exception e) {
			log.error("Error consultando entrega por id: " + e.getMessage());
		}

		try {
			operatorDto = operatorBusiness.getOperatorById(operatorId);
		} catch (Exception e) {
			log.error("Error consultando operador por id: " + e.getMessage());
		}

		if (deliveryDto == null) {
			throw new BusinessException("No se ha encontrado la entrega.");
		}

		if (operatorDto == null) {
			throw new BusinessException("No se ha encontrado el operador.");
		}

		if (!deliveryDto.getOperator().getId().equals(operatorId)) {
			throw new BusinessException("La entrega no pertenece al operador.");
		}

		for (MicroserviceSupplyDeliveryDto supplyDeliveryDto : deliveryDto.getSupplies()) {
			if (!supplyDeliveryDto.getDownloaded()) {
				throw new BusinessException(
						"No se puede generar el reporte, porque no se han descargado todo los insumos de la entrega.");
			}
		}

		if (deliveryDto.getDownloadReportUrl() != null && !deliveryDto.getDownloadReportUrl().isEmpty()) {
			return deliveryDto.getDownloadReportUrl();
		}

		MunicipalityDto municipalityDto = municipalityBusiness.getMunicipalityByCode(deliveryDto.getMunicipalityCode());
		if (!(municipalityDto instanceof MunicipalityDto)) {
			throw new BusinessException("No se ha encontrado el municipio.");
		}

		MicroserviceManagerDto managerDto = managerBusiness.getManagerById(deliveryDto.getManagerCode());
		if (!(managerDto instanceof MicroserviceManagerDto)) {
			throw new BusinessException("No se ha encontrado el gestor.");
		}

		String format = "yyyy-MM-dd hh:mm:ss";

		// configuration params
		String namespace = "/" + deliveryDto.getMunicipalityCode() + "/reportes/entregas/" + deliveryDto.getId() + "/";
		String dateCreation = DateTool.formatDate(new Date(), format);
		String dateDelivery = DateTool.formatDate(deliveryDto.getCreatedAt(), format);
		String departmentName = municipalityDto.getDepartment().getName();
		String managerName = managerDto.getName();
		String municipalityCode = municipalityDto.getCode();
		String municipalityName = municipalityDto.getName();
		String observations = deliveryDto.getObservations();
		String operatorName = operatorDto.getName();

		List<MicroserviceDownloadedSupplyDto> supplies = new ArrayList<MicroserviceDownloadedSupplyDto>();
		for (MicroserviceSupplyDeliveryDto supplyDeliveryDto : deliveryDto.getSupplies()) {
			String supplyName = "";
			String providerName = "";
			MicroserviceSupplyDto supplyDto = supplyBusiness.getSupplyById(supplyDeliveryDto.getSupplyCode());
			if (supplyDto != null) {
				supplyName = (supplyDto.getTypeSupply() != null) ? supplyDto.getTypeSupply().getName() : "";
				providerName = (supplyDto.getTypeSupply() != null) ? supplyDto.getTypeSupply().getProvider().getName()
						: "";
			}

			String downloadedBy = "";
			MicroserviceUserDto userDto = userBusiness.getUserById(supplyDeliveryDto.getDownloadedBy());
			if (userDto != null) {
				downloadedBy = userDto.getFirstName() + " " + userDto.getLastName();
			}

			supplies.add(new MicroserviceDownloadedSupplyDto(supplyName,
					DateTool.formatDate(supplyDeliveryDto.getDownloadedAt(), format), downloadedBy, providerName));
		}

		MicroserviceReportInformationDto report = reportBusiness.generateReportDownloadSupply(namespace, dateCreation,
				dateDelivery, deliveryId.toString(), departmentName, managerName, municipalityCode, municipalityName,
				observations, operatorName, supplies);

		// update url report
		operatorBusiness.updateReportDelivery(deliveryId, report.getUrlReport());

		return report.getUrlReport();
	}

}
