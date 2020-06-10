package com.ai.st.microservice.workspaces.business;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceReportInformationDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class WorkspaceOperatorBusiness {

	private final Logger log = LoggerFactory.getLogger(WorkspaceOperatorBusiness.class);

	@Autowired
	private OperatorBusiness operatorBusiness;

	@Autowired
	private ReportBusiness reportBusiness;

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

		// configuration params
		String title = "Hola mundo";
		String namespace = "/" + deliveryDto.getMunicipalityCode() + "/reportes/entregas/" + deliveryDto.getId() + "/";

		MicroserviceReportInformationDto report = reportBusiness.generateReportDownloadSupplyIndividual(title,
				namespace);

		// update url report
		operatorBusiness.updateSupplyDeliveredReportURL(deliveryId, supplyId, report.getUrlReport());

		return report.getUrlReport();
	}

	public String generateReportDownloadSupplyTotal(Long operatorId, Long deliveryId) throws BusinessException {

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

		if (!deliveryDto.getIsActive()) {
			throw new BusinessException("La entrega no se encuentra activa para generar el reporte solicitado.");
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

		// configuration params
		String title = "Hola mundo";
		String namespace = "/" + deliveryDto.getMunicipalityCode() + "/reportes/entregas/" + deliveryDto.getId() + "/";

		MicroserviceReportInformationDto report = reportBusiness.generateReportDownloadSupplyTotal(title, namespace);

		// update url report
		operatorBusiness.updateReportDelivery(deliveryId, report.getUrlReport());

		return report.getUrlReport();
	}

}
