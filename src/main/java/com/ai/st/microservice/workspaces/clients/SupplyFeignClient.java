package com.ai.st.microservice.workspaces.clients;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceDataPaginatedDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceCreateSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceUpdateSupplyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "st-microservice-supplies", configuration = SupplyFeignClient.Configuration.class)
public interface SupplyFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "/api/supplies/v1/supplies", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceSupplyDto createSupply(@RequestBody MicroserviceCreateSupplyDto createSupply);

	@GetMapping("/api/supplies/v1/supplies/municipality/{municipalityId}")
	public List<MicroserviceSupplyDto> getSuppliesByMunicipalityCode(@PathVariable String municipalityId,
			@RequestParam(name = "states", required = false) List<Long> states);

	@GetMapping("/api/supplies/v1/supplies/municipality/{municipalityId}")
	public MicroserviceDataPaginatedDto getSuppliesByMunicipalityCodeByFilters(@PathVariable String municipalityId,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "requests", required = false) List<Long> requests,
			@RequestParam(name = "states", required = false) List<Long> states);

	@GetMapping("/api/supplies/v1/supplies/{supplyId}")
	public MicroserviceSupplyDto findSupplyById(@PathVariable Long supplyId);

	@DeleteMapping("/api/supplies/v1/supplies/{supplyId}")
	public void deleteSupplyById(@PathVariable Long supplyId);

	@RequestMapping(method = RequestMethod.PUT, value = "/api/supplies/v1/supplies/{supplyId}", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceSupplyDto updateSupply(@PathVariable Long supplyId,
			@RequestBody MicroserviceUpdateSupplyDto updateSupply) throws BusinessException;

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
