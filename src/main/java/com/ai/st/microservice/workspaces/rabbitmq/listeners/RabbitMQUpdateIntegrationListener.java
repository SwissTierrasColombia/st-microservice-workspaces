package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationStatDto;

@Component
public class RabbitMQUpdateIntegrationListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@RabbitListener(queues = "${st.rabbitmq.queueUpdateIntegration.queue}")
	public void recievedMessage(MicroserviceIntegrationStatDto integrationStats) {

		

	}

}
