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

	@Value("${st.rabbitmq.queueUpdateIntegration.queue}")
	public String queueUpdateIntegrationsName;

	@Value("${st.rabbitmq.queueUpdateIntegration.exchange}")
	public String exchangeUpdateIntegrationsName;

	@Value("${st.rabbitmq.queueUpdateIntegration.routingkey}")
	public String routingkeyUpdateIntegrationsName;

	@Value("${st.rabbitmq.queueUpdateExport.queue}")
	public String queueUpdateExportName;

	@Value("${st.rabbitmq.queueUpdateExport.exchange}")
	public String exchangeUpdateExportName;

	@Value("${st.rabbitmq.queueUpdateExport.routingkey}")
	public String routingkeyUpdateExportName;

	@Value("${st.rabbitmq.queueUpdateStateSupply.queue}")
	public String queueUpdateStateSupplyName;

	@Value("${st.rabbitmq.queueUpdateStateSupply.exchange}")
	public String exchangeUpdateStateSupplyName;

	@Value("${st.rabbitmq.queueUpdateStateSupply.routingkey}")
	public String routingkeyUpdateStateSupplyName;

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
	public Queue queueUpdateExports() {
		return new Queue(queueUpdateExportName, false);
	}

	@Bean
	public DirectExchange exchangeUpdateExports() {
		return new DirectExchange(exchangeUpdateExportName);
	}

	@Bean
	public Binding bindingQueueUpdateExports() {
		return BindingBuilder.bind(queueUpdateExports()).to(exchangeUpdateExports()).with(routingkeyUpdateExportName);
	}

	@Bean
	public Queue queueUpdateStateSupply() {
		return new Queue(queueUpdateStateSupplyName, false);
	}

	@Bean
	public DirectExchange exchangeUpdateStateSupply() {
		return new DirectExchange(exchangeUpdateStateSupplyName);
	}

	@Bean
	public Binding bindingQueueUpdateStateSupply() {
		return BindingBuilder.bind(queueUpdateStateSupply()).to(exchangeUpdateStateSupply())
				.with(routingkeyUpdateStateSupplyName);
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
