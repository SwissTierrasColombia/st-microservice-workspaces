package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import com.ai.st.microservice.common.dto.ili.MicroserviceResultImportDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceSupplyRevisionDto;

import com.ai.st.microservice.workspaces.business.ProviderBusiness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQResultImportListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ProviderBusiness providerBusiness;

    public RabbitMQResultImportListener(ProviderBusiness providerBusiness) {
        this.providerBusiness = providerBusiness;
    }

    @RabbitListener(queues = "${st.rabbitmq.queueResultImport.queue}", concurrency = "${st.rabbitmq.queueResultImport.concurrency}")
    public void updateIntegration(MicroserviceResultImportDto resultDto) {

        log.info("procesando resultado de la importación ... " + resultDto.getReference());

        try {

            String[] reference = resultDto.getReference().split("-");

            String typeResult = reference[0];

            if (typeResult.equalsIgnoreCase("import")) {

                Long supplyRequestedId = Long.parseLong(reference[1]);
                Long requestId = Long.parseLong(reference[2]);

                MicroserviceSupplyRevisionDto supplyRevisionDto = providerBusiness
                        .getSupplyRevisionFromSupplyRequested(supplyRequestedId);

                if (supplyRevisionDto == null || !resultDto.getResult()) {
                    providerBusiness.updateStateToSupplyRequested(requestId, supplyRequestedId,
                            ProviderBusiness.SUPPLY_REQUESTED_STATE_PENDING_REVIEW);
                }

                if (resultDto.getResult()) {

                    providerBusiness.updateStateToSupplyRequested(requestId, supplyRequestedId,
                            ProviderBusiness.SUPPLY_REQUESTED_STATE_IN_REVIEW);

                } else {

                    providerBusiness.deleteSupplyRevision(supplyRequestedId, supplyRevisionDto.getId());

                }

                log.info("se realizaron los procesos del resultado: " + resultDto.getResult());

            }

        } catch (Exception e) {
            log.error("Ha ocurrido un error actualizando el resultado de la importación: " + e.getMessage());
        }

    }

}
