package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ai.st.microservice.workspaces.business.ProviderBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceValidationDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserivceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.services.RabbitMQSenderService;

@Component
public class RabbitMQUpdateStateSupplyListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RabbitMQSenderService rabbitMQService;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private ProviderFeignClient providerClient;

	@RabbitListener(queues = "${st.rabbitmq.queueUpdateStateSupply.queue}", concurrency = "${st.rabbitmq.queueUpdateStateSupply.concurrency}")
	public void updateIntegration(MicroserviceValidationDto validationDto) {

		try {

			Long supplyRequestedStateId = null;

			if (validationDto.getIsValid()) {

				MicroserviceRequestDto requestDto = providerClient.findRequestById(validationDto.getRequestId());
				MicroserviceProviderDto providerDto = requestDto.getProvider();

				MicroserivceSupplyRequestedDto supplyRequestedDto = requestDto.getSuppliesRequested().stream()
						.filter(supply -> supply.getId() == validationDto.getSupplyRequestedId()).findAny()
						.orElse(null);

				supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED;

				// save file with microservice file manager
				String urlBase = "/" + requestDto.getMunicipalityCode().replace(" ", "_") + "/insumos/proveedores/"
						+ providerDto.getName().replace(" ", "_") + "/"
						+ supplyRequestedDto.getTypeSupply().getName().replace(" ", "_");

				List<String> urls = new ArrayList<String>();
				String urlDocumentaryRepository = rabbitMQService
						.sendFile(StringUtils.cleanPath(validationDto.getFilenameTemporal()), urlBase);
				urls.add(urlDocumentaryRepository);

				supplyBusiness.createSupply(requestDto.getMunicipalityCode(), validationDto.getObservations(),
						supplyRequestedDto.getTypeSupply().getId(), urls, null, validationDto.getRequestId(),
						validationDto.getUserCode(), providerDto.getId(), null);

				// Update request
				MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();
				updateSupply.setDelivered(validationDto.getIsValid());
				updateSupply.setSupplyRequestedStateId(supplyRequestedStateId);
				updateSupply.setJustification("");
				providerClient.updateSupplyRequested(validationDto.getRequestId(), supplyRequestedDto.getId(),
						updateSupply);

			} else {
				supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_REJECTED;
			}

			// update request
			MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();
			updateSupply.setDelivered(validationDto.getIsValid());
			updateSupply.setSupplyRequestedStateId(supplyRequestedStateId);
			updateSupply.setJustification("");
			providerClient.updateSupplyRequested(validationDto.getRequestId(), validationDto.getSupplyRequestedId(),
					updateSupply);

			log.info("Se ha actualizado el estado del insumo");

		} catch (Exception e) {
			log.error("Ha ocurrido un error actualizando el estado del insumo: " + e.getMessage());
		}

	}

}
