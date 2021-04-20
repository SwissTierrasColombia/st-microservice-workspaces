package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.business.NotificationBusiness;
import com.ai.st.microservice.workspaces.business.ProviderBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceValidationDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;

@Component
public class RabbitMQUpdateStateSupplyListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProviderFeignClient providerClient;

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

            MicroserviceUpdateSupplyRequestedDto updateSupply = new MicroserviceUpdateSupplyRequestedDto();

            Long supplyRequestedStateId;

            MicroserviceRequestDto requestDto = providerClient.findRequestById(validationDto.getRequestId());

            if (validationDto.getIsValid()) {

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

                MicroserviceUserDto userDto = userBusiness.getUserById(supplyRequestedDto.getDeliveredBy());
                if (userDto != null && userDto.getEnabled()) {
                    notificationBusiness.sendNotificationLoadOfInputs(userDto.getEmail(), userDto.getId(),
                            validationDto.getIsValid(), municipalityEntity.getName(),
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
            updateSupply.setValidated(validationDto.getGeometryValidated());
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
