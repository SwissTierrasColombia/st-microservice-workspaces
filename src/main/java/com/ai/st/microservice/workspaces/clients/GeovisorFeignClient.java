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

import com.ai.st.microservice.workspaces.dto.geovisor.MicroserviceDataMapDto;
import com.ai.st.microservice.workspaces.dto.geovisor.MicroserviceSetupMapDto;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "data", configuration = GeovisorFeignClient.Configuration.class, url = "pending")
public interface GeovisorFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "/users/test", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceDataMapDto setupMap(@RequestBody MicroserviceSetupMapDto data);

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
