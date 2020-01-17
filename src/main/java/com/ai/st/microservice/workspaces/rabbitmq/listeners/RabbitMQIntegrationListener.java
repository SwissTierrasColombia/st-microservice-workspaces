package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQIntegrationListener {

	@RabbitListener(queues = "${st.rabbitmq.queueIntegrations.queue}")
	public void recievedMessage(String message) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}

		System.out.println("receive message: " + message);
	}

}
