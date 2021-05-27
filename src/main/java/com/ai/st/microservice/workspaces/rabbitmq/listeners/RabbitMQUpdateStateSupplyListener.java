package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.ili.MicroserviceValidationDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceUpdateSupplyRequestedDto;

import com.ai.st.microservice.workspaces.business.NotificationBusiness;
import com.ai.st.microservice.workspaces.business.ProviderBusiness;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

@Component
public class RabbitMQUpdateStateSupplyListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ProviderFeignClient providerClient;
    private final NotificationBusiness notificationBusiness;
    private final IMunicipalityService municipalityService;
    private final AdministrationBusiness administrationBusiness;

    public RabbitMQUpdateStateSupplyListener(ProviderFeignClient providerClient, NotificationBusiness notificationBusiness,
                                             IMunicipalityService municipalityService, AdministrationBusiness administrationBusiness) {
        this.providerClient = providerClient;
        this.notificationBusiness = notificationBusiness;
        this.municipalityService = municipalityService;
        this.administrationBusiness = administrationBusiness;
    }

    @RabbitListener(queues = "${st.rabbitmq.queueUpdateStateSupply.queue}", concurrency = "${st.rabbitmq.queueUpdateStateSupply.concurrency}")
    public void updateIntegration(MicroserviceValidationDto validationDto) {

        try {

            log.info("updating supply ... " + validationDto.getSupplyRequestedId());

            MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();

            Long supplyRequestedStateId;

            MicroserviceRequestDto requestDto = providerClient.findRequestById(validationDto.getRequestId());

            boolean xtfAccept = validationDto.getIsValid() || validationDto.getSkipErrors();
            if (xtfAccept) {

                supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED;
                updateSupply.setUrl(validationDto.getFilenameTemporal());

            } else {
                supplyRequestedStateId = ProviderBusiness.SUPPLY_REQUESTED_STATE_REJECTED;

                try {
                    FileUtils.deleteQuietly(new File(validationDto.getFilenameTemporal()));
                } catch (Exception e) {
                    log.error("No se ha podido eliminar el insumo rechazado: " + e.getMessage());
                }

            }

            // send notification provider

            try {

                MicroserviceSupplyRequestedDto supplyRequestedDto = requestDto.getSuppliesRequested().stream()
                        .filter(supply -> supply.getId().equals(validationDto.getSupplyRequestedId())).findAny()
                        .orElse(null);

                MunicipalityEntity municipalityEntity = municipalityService
                        .getMunicipalityByCode(requestDto.getMunicipalityCode());

                MicroserviceUserDto userDto = administrationBusiness.getUserById(supplyRequestedDto.getDeliveredBy());
                if (userDto != null && userDto.getEnabled()) {
                    notificationBusiness.sendNotificationLoadOfInputs(userDto.getEmail(), userDto.getId(),
                            xtfAccept, municipalityEntity.getName(),
                            municipalityEntity.getDepartment().getName(), validationDto.getRequestId().toString(),
                            new Date(), "");
                }

            } catch (Exception e) {
                log.error("Error enviando notificación para informar de la carga del insumo: " + e.getMessage());
            }

            // update request
            updateSupply.setDelivered(validationDto.getIsValid());
            updateSupply.setSupplyRequestedStateId(supplyRequestedStateId);
            updateSupply.setJustification("");
            updateSupply.setDeliveryBy(null);
            updateSupply.setObservations(validationDto.getObservations());
            updateSupply.setValidated(validationDto.getIsValid()); // is xtf valid?
            updateSupply.setLog(validationDto.getLog());
            if (validationDto.getErrors().size() > 0) {
                StringBuilder errors = new StringBuilder();
                for (String error : validationDto.getErrors()) {
                    errors.append(error).append("\n");
                }
                updateSupply.setErrors(errors.toString());
            }

            providerClient.updateSupplyRequested(validationDto.getRequestId(), validationDto.getSupplyRequestedId(),
                    updateSupply);

            log.info("Se ha actualizado el estado del insumo");

        } catch (Exception e) {
            log.error("Ha ocurrido un error actualizando el estado del insumo: " + e.getMessage());
        }

    }

}
