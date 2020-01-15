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

	@Value("${st.rabbitmq.exchange}")
	private String exchange;

	@Value("${st.rabbitmq.routingkey}")
	private String routingkey;

	public String sendFile(byte[] file, String filename, String namespace, String driver) {

		String url = null;

		UploadFileMessageDto message = new UploadFileMessageDto(filename, namespace, driver, file);
		url = (String) rabbitTemplate.convertSendAndReceive(exchange, routingkey, message);

		return url;
	}

}
