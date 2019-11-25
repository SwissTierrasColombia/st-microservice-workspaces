package com.ai.st.microservice.workspaces.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;

import feign.Feign;
import feign.codec.Encoder;

import feign.form.spring.SpringFormEncoder;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

@FeignClient(name = "st-microservice-providers", configuration = ProviderFeignClient.Configuration.class)
public interface ProviderFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "/api/providers-supplies/v1/requests", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceRequestDto createRequest(@RequestBody MicroserviceCreateRequestDto request);

	@GetMapping("/api/providers-supplies/v1/users/{userCode}/providers")
	public MicroserviceProviderDto findByUserCode(@PathVariable Long userCode);

	@GetMapping("/api/providers-supplies/v1/providers/{providerId}/requests")
	public List<MicroserviceRequestDto> getRequestsByProvider(@PathVariable Long providerId,
			@RequestParam(required = false, name = "state") Long requestStateId);

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
