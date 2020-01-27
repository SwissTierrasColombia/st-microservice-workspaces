package com.ai.st.microservice.workspaces.clients;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "st-microservice-supplies", configuration = SupplyFeignClient.Configuration.class)
public interface SupplyFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "/api/supplies/v1/supplies", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceSupplyDto createSupply(@RequestBody MicroserviceCreateSupplyDto createSupply);

	@GetMapping("/api/supplies/v1/supplies/municipality/{municipalityId}")
	public List<MicroserviceSupplyDto> getSuppliesByMunicipalityCode(@PathVariable String municipalityId);

	@GetMapping("/api/supplies/v1/supplies/{supplyId}")
	public MicroserviceSupplyDto findSupplyById(@PathVariable Long supplyId);

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
