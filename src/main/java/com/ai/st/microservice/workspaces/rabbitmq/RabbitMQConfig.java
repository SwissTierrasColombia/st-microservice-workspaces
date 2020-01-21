package com.ai.st.microservice.workspaces.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	@Value("${st.rabbitmq.queueFiles.queue}")
	public String queueFilesName;

	@Value("${st.rabbitmq.queueFiles.exchange}")
	public String exchangeFilesName;

	@Value("${st.rabbitmq.queueFiles.routingkey}")
	public String routingkeyFilesName;

	@Value("${st.rabbitmq.queueIntegrations.queue}")
	public String queueIntegrationsName;

	@Value("${st.rabbitmq.queueIntegrations.exchange}")
	public String exchangeIntegrationsName;

	@Value("${st.rabbitmq.queueIntegrations.routingkey}")
	public String routingkeyIntegrationsName;

	@Value("${st.rabbitmq.queueUpdateIntegration.queue}")
	public String queueUpdateIntegrationsName;

	@Value("${st.rabbitmq.queueUpdateIntegration.exchange}")
	public String exchangeUpdateIntegrationsName;

	@Value("${st.rabbitmq.queueUpdateIntegration.routingkey}")
	public String routingkeyUpdateIntegrationsName;

	@Bean
	public Queue queueFiles() {
		return new Queue(queueFilesName, false);
	}

	@Bean
	public DirectExchange exchangeFiles() {
		return new DirectExchange(exchangeFilesName);
	}

	@Bean
	public Binding bindingQueueFiles() {
		return BindingBuilder.bind(queueFiles()).to(exchangeFiles()).with(routingkeyFilesName);
	}

	@Bean
	public Queue queueIntegrations() {
		return new Queue(queueIntegrationsName, false);
	}

	@Bean
	public DirectExchange exchangeIntegrations() {
		return new DirectExchange(exchangeIntegrationsName);
	}

	@Bean
	public Binding bindingQueueIntegrations() {
		return BindingBuilder.bind(queueIntegrations()).to(exchangeIntegrations()).with(routingkeyIntegrationsName);
	}

	@Bean
	public Queue queueUpdateIntegrations() {
		return new Queue(queueUpdateIntegrationsName, false);
	}

	@Bean
	public DirectExchange exchangeUpdateIntegrations() {
		return new DirectExchange(exchangeUpdateIntegrationsName);
	}

	@Bean
	public Binding bindingQueueUpdateIntegrations() {
		return BindingBuilder.bind(queueUpdateIntegrations()).to(exchangeUpdateIntegrations())
				.with(routingkeyUpdateIntegrationsName);
	}

	@Bean
	public Jackson2JsonMessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

}
