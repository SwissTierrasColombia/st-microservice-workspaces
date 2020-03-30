package com.ai.st.microservice.workspaces.services;

import java.io.File;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.dto.rabbitmq.UploadFileMessageDto;

@Service
public class RabbitMQSenderService {

	@Autowired
	private AmqpTemplate rabbitTemplate;

	@Value("${st.filesDirectory}")
	public String filesDirectory;

	@Value("${st.rabbitmq.queueFiles.exchange}")
	public String exchangeFilesName;

	@Value("${st.rabbitmq.queueFiles.routingkey}")
	public String routingkeyFilesName;

	public String sendFile(String filename, String namespace, boolean zip) {

		String fileNameRandom = RandomStringUtils.random(14, true, false);

		UploadFileMessageDto message = new UploadFileMessageDto(filename, namespace, "Local", new byte[1], zip,
				fileNameRandom);

		rabbitTemplate.convertAndSend(exchangeFilesName, routingkeyFilesName, message);

		String url = null;

		if (zip) {
			url = filesDirectory + namespace + File.separatorChar + fileNameRandom + ".zip";
		} else {
			url = filesDirectory + namespace + File.separatorChar + filename;
		}

		return url;
	}

}
