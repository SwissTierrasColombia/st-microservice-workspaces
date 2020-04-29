package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ai.st.microservice.workspaces.business.NotificationBusiness;
import com.ai.st.microservice.workspaces.business.ProviderBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceValidationDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
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

	@Autowired
	private ProviderBusiness providerBusiness;

	@Autowired
	private UserBusiness userBusiness;

	@Autowired
	private NotificationBusiness notificationBusiness;

	@Autowired
	private IMunicipalityService municipalityService;

	@RabbitListener(queues = "${st.rabbitmq.queueUpdateStateSupply.queue}", concurrency = "${st.rabbitmq.queueUpdateStateSupply.concurrency}")
	public void updateIntegration(MicroserviceValidationDto validationDto) {

		try {

			log.info("updating supply ... " + validationDto.getSupplyRequestedId());

			Long supplyRequestedStateId = null;

			MicroserviceRequestDto requestDto = providerClient.findRequestById(validationDto.getRequestId());
			MicroserviceProviderDto providerDto = requestDto.getProvider();

			if (validationDto.getIsValid()) {

				MicroserviceSupplyRequestedDto supplyRequestedDto = requestDto.getSuppliesRequested().stream()
						.filter(supply -> supply.getId().equals(validationDto.getSupplyRequestedId())).findAny()
						.orElse(null);

				supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED;

				// save file with microservice file manager
				String urlBase = "/" + requestDto.getMunicipalityCode().replace(" ", "_") + "/insumos/proveedores/"
						+ providerDto.getName().replace(" ", "_") + "/"
						+ supplyRequestedDto.getTypeSupply().getName().replace(" ", "_");

				String fileName = validationDto.getFilenameTemporal();
				String fileExtension = FilenameUtils.getExtension(fileName);
				Boolean zipFile = fileExtension.equalsIgnoreCase("zip") ? false : true;

				List<String> urls = new ArrayList<String>();
				String urlDocumentaryRepository = rabbitMQService.sendFile(StringUtils.cleanPath(fileName), urlBase,
						zipFile);
				urls.add(urlDocumentaryRepository);

				supplyBusiness.createSupply(requestDto.getMunicipalityCode(), validationDto.getObservations(),
						supplyRequestedDto.getTypeSupply().getId(), urls, null, validationDto.getRequestId(),
						validationDto.getUserCode(), providerDto.getId(), null, supplyRequestedDto.getModelVersion());

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

			// send notification provider

			try {

				MunicipalityEntity municipalityEntity = municipalityService
						.getMunicipalityByCode(requestDto.getMunicipalityCode());

				List<MicroserviceProviderUserDto> providerUsers = providerBusiness
						.getUsersByProvider(providerDto.getId(), null);

				for (MicroserviceProviderUserDto providerUser : providerUsers) {

					MicroserviceUserDto userDto = userBusiness.getUserById(providerUser.getUserCode());

					notificationBusiness.sendNotificationLoadOfInputs(userDto.getEmail(), userDto.getId(),
							validationDto.getIsValid(), municipalityEntity.getName(),
							municipalityEntity.getDepartment().getName(), validationDto.getRequestId().toString(),
							new Date(), "");
				}

			} catch (Exception e) {
				log.error("Error enviando notificación para informar de la carga del insumo: " + e.getMessage());
			}

			// TODO: Send notification manager (validation)

			// update request
			MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();
			updateSupply.setDelivered(validationDto.getIsValid());
			updateSupply.setSupplyRequestedStateId(supplyRequestedStateId);
			updateSupply.setJustification("");
			updateSupply.setDeliveryBy(null);
			providerClient.updateSupplyRequested(validationDto.getRequestId(), validationDto.getSupplyRequestedId(),
					updateSupply);

			log.info("Se ha actualizado el estado del insumo");

		} catch (Exception e) {
			log.error("Ha ocurrido un error actualizando el estado del insumo: " + e.getMessage());
		}

	}

}
