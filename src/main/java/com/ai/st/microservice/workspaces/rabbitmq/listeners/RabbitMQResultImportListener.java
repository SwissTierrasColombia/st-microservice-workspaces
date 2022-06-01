package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import com.ai.st.microservice.common.dto.ili.MicroserviceResultImportDto;

import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQResultImportListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @RabbitListener(queues = "${st.rabbitmq.queueResultImport.queue}", concurrency = "${st.rabbitmq.queueResultImport.concurrency}")
    public void resultImportProcess(MicroserviceResultImportDto resultDto) {

        log.info("procesando resultado de la importación ... " + resultDto.getReference());

        try {

            String[] reference = resultDto.getReference().split("-");

            String typeResult = reference[0];

            if (typeResult.equalsIgnoreCase("import")) {

            }

        } catch (Exception e) {
            String messageError = String.format(
                    "Error procesando el resultado de la importación con referencia %s : %s", resultDto.getReference(),
                    e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
        }

    }

}
