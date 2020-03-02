package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.OperatorFeignClient;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateDeliverySupplyDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceUpdateDeliveredSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;

@Component
public class OperatorBusiness {

	private final Logger log = LoggerFactory.getLogger(OperatorBusiness.class);

	@Autowired
	private OperatorFeignClient operatorClient;

	@Autowired
	private SupplyBusiness supplyBusiness;

	public MicroserviceDeliveryDto createDelivery(Long operatorId, Long managerCode, String municipalityCode,
			String observations, List<MicroserviceCreateDeliverySupplyDto> supplies)
			throws DisconnectedMicroserviceException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {

			MicroserviceCreateDeliveryDto createDeliveryDto = new MicroserviceCreateDeliveryDto();
			createDeliveryDto.setManagerCode(managerCode);
			createDeliveryDto.setMunicipalityCode(municipalityCode);
			createDeliveryDto.setObservations(observations);
			createDeliveryDto.setSupplies(supplies);

			deliveryDto = operatorClient.createDelivery(operatorId, createDeliveryDto);

		} catch (Exception e) {
			log.error("Error creando la entrega: " + e.getMessage());
			throw new DisconnectedMicroserviceException("No se ha podido crear la entrega.");
		}

		return deliveryDto;
	}

	public List<MicroserviceDeliveryDto> getDeliveriesByOperator(Long operatorId, String municipalityCode)
			throws BusinessException {

		List<MicroserviceDeliveryDto> deliveries = new ArrayList<>();

		try {
			deliveries = operatorClient.findDeliveriesByOperator(operatorId, municipalityCode);
		} catch (Exception e) {
			log.error("Error consultando las entregas: " + e.getMessage());
		}

		return deliveries;
	}

	public List<MicroserviceDeliveryDto> getDeliveriesActivesByOperator(Long operatorId) throws BusinessException {

		List<MicroserviceDeliveryDto> deliveries = new ArrayList<>();

		try {
			deliveries = operatorClient.findDeliveriesActivesByOperator(operatorId, true);

			for (MicroserviceDeliveryDto deliveryDto : deliveries) {

				List<MicroserviceSupplyDeliveryDto> supplyDeliveriesDto = deliveryDto.getSupplies();

				for (MicroserviceSupplyDeliveryDto supplyDeliveryDto : supplyDeliveriesDto) {

					try {

						MicroserviceSupplyDto supplyDto = supplyBusiness
								.getSupplyById(supplyDeliveryDto.getSupplyCode());
						supplyDeliveryDto.setSupply(supplyDto);

					} catch (Exception e) {
						log.error("Error consultando insumo: " + e.getMessage());
					}

				}

			}

		} catch (Exception e) {
			log.error("Error consultando las entregas activas: " + e.getMessage());
		}

		return deliveries;
	}

	public MicroserviceDeliveryDto updateSupplyDeliveredDownloaded(Long deliveryId, Long supplyId)
			throws DisconnectedMicroserviceException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {

			MicroserviceUpdateDeliveredSupplyDto supplyDelivered = new MicroserviceUpdateDeliveredSupplyDto();
			supplyDelivered.setDownloaded(true);

			deliveryDto = operatorClient.updateSupplyDelivered(deliveryId, supplyId, supplyDelivered);

		} catch (Exception e) {
			log.error("Error actualizando la fecha de descarga del insumo.");
		}

		return deliveryDto;
	}

}
