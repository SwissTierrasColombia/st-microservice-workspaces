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

	public String sendFile(String filename, String namespace) {

		String url = null;

		UploadFileMessageDto message = new UploadFileMessageDto(filename, namespace, "Local", new byte[1]);
		url = (String) rabbitTemplate.convertSendAndReceive(exchangeFilesName, routingkeyFilesName, message);

		return url;
	}

}
