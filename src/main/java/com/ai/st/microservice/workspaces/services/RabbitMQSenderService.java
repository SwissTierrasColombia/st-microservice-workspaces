package com.ai.st.microservice.workspaces.services;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.dto.rabbitmq.UploadFileMessageDto;

@Service
public class RabbitMQSenderService {

	@Autowired
	private AmqpTemplate rabbitTemplate;

	@Value("${st.rabbitmq.queueFiles.exchange}")
	public String exchangeFilesName;

	@Value("${st.rabbitmq.queueFiles.routingkey}")
	public String routingkeyFilesName;

	@Value("${st.rabbitmq.queueIntegrations.exchange}")
	public String exchangeIntegrationsName;

	@Value("${st.rabbitmq.queueIntegrations.routingkey}")
	public String routingkeyIntegrationsName;

	public String sendFile(byte[] file, String filename, String namespace, String driver) {

		String url = null;

		UploadFileMessageDto message = new UploadFileMessageDto(filename, namespace, driver, file);
		url = (String) rabbitTemplate.convertSendAndReceive(exchangeFilesName, routingkeyFilesName, message);

		return url;
	}

	public void sendMessage(String message) {

		rabbitTemplate.convertAndSend(exchangeIntegrationsName, routingkeyIntegrationsName, message);

		System.out.println("send message: " + message);
	}

}
