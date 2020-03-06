package com.ai.st.microservice.workspaces.clients;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationNewUserDto;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "st-microservice-notifier", configuration = NotifierFeignClient.Configuration.class)
public interface NotifierFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "/api/notifier/v1/notify/new_user", consumes = APPLICATION_JSON_VALUE)
	public void creationUser(@RequestBody MicroserviceNotificationNewUserDto data);

	class Configuration {

		@Bean
		Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
			return new SpringFormEncoder(new SpringEncoder(converters));
		}

		@Bean
		@Scope("prototype")
		public Feign.Builder feignBuilder() {
			return Feign.builder();
		}

	}

}
