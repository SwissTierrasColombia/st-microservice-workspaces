package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import com.ai.st.microservice.common.dto.ili.MicroserviceResultExportDto;

import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQResultExportListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @RabbitListener(queues = "${st.rabbitmq.queueResultExport.queue}", concurrency = "${st.rabbitmq.queueResultExport.concurrency}")
    public void updateResultExport(MicroserviceResultExportDto resultDto) {

        log.info("procesando resultado de la exportación ... " + resultDto.getReference());

        try {

            String[] reference = resultDto.getReference().split("-");
            String typeResult = reference[0];

            if (typeResult.equalsIgnoreCase("export")) {

            }

        } catch (Exception e) {
            String messageError = String.format(
                    "Error procesando el resultado de la exportación con referencia %s : %s", resultDto.getReference(),
                    e.getMessage());
            SCMTracing.sendError(messageError);
        }

    }

}
