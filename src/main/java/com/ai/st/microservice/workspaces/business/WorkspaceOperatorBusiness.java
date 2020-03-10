package com.ai.st.microservice.workspaces.business;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class WorkspaceOperatorBusiness {

	private final Logger log = LoggerFactory.getLogger(WorkspaceOperatorBusiness.class);

	@Autowired
	private OperatorBusiness operatorBusiness;

	public MicroserviceDeliveryDto getDeliveryFromSupply(Long operatorCode, Long supplyCode) throws BusinessException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {

			List<MicroserviceDeliveryDto> deliveries = operatorBusiness.getDeliveriesActivesByOperator(operatorCode);

			for (MicroserviceDeliveryDto delivery : deliveries) {

				MicroserviceSupplyDeliveryDto supplyDto = delivery.getSupplies().stream()
						.filter(s -> s.getSupplyCode() == supplyCode).findAny().orElse(null);

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

	public MicroserviceDeliveryDto registerDownloadSupply(MicroserviceDeliveryDto deliveryDto, Long supplyCode) {

		try {

			MicroserviceSupplyDeliveryDto supplyDto = deliveryDto.getSupplies().stream()
					.filter(s -> s.getSupplyCode() == supplyCode).findAny().orElse(null);

			if (supplyDto != null && !supplyDto.getDownloaded()) {
				deliveryDto = operatorBusiness.updateSupplyDeliveredDownloaded(deliveryDto.getId(), supplyCode);
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
			
		}

		if (deliveryDto == null) {
			throw new BusinessException("No se ha encontrado la entrega.");
		}

		if (deliveryDto.getOperator().getId() != operatorId) {
			throw new BusinessException("La entrega no pertenece al operador.");
		}

		try {
			deliveryDto = operatorBusiness.disableDelivery(deliveryId);
		} catch (Exception e) {
			throw new BusinessException("No se ha podido desactivar la entrega.");
		}

		return deliveryDto;
	}

}
