package com.ai.st.microservice.workspaces.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.st.microservice.workspaces.dto.providers.MicroserviceAddUserToProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceCreateRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceTypeSupplyDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

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
	public MicroserviceRequestDto createRequest(@RequestBody MicroserviceCreateRequestDto request)
			throws BusinessException;

	@GetMapping("/api/providers-supplies/v1/users/{userCode}/providers")
	public MicroserviceProviderDto findByUserCode(@PathVariable Long userCode);

	@GetMapping("/api/providers-supplies/v1/providers/{providerId}")
	public MicroserviceProviderDto findById(@PathVariable(name = "providerId", required = true) Long providerId);

	@GetMapping("/api/providers-supplies/v1/providers/{providerId}/requests")
	public List<MicroserviceRequestDto> getRequestsByProvider(@PathVariable Long providerId,
			@RequestParam(required = false, name = "state") Long requestStateId);

	@GetMapping("/api/providers-supplies/v1/requests/{requestId}")
	public MicroserviceRequestDto findRequestById(@PathVariable Long requestId);

	@GetMapping("/api/providers-supplies/v1/providers/{providerId}/users")
	public List<MicroserviceProviderUserDto> findUsersByProviderId(@PathVariable Long providerId)
			throws BusinessException;

	@RequestMapping(method = RequestMethod.PUT, value = "/api/providers-supplies/v1/requests/{requestId}/supplies/{supplyRequestedId}", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceRequestDto updateSupplyRequested(@PathVariable Long requestId,
			@PathVariable Long supplyRequestedId, @RequestBody MicroserviceUpdateSupplyRequestedDto updateSupply);

	@PutMapping("/api/providers-supplies/v1/requests/{requestId}/delivered")
	public MicroserviceRequestDto closeRequest(@PathVariable Long requestId);

	@RequestMapping(method = RequestMethod.POST, value = "/api/providers-supplies/v1/users", consumes = APPLICATION_JSON_VALUE)
	public List<MicroserviceProviderUserDto> addUserToProvide(@RequestBody MicroserviceAddUserToProviderDto data);

	@GetMapping("/api/providers-supplies/v1/types-supplies/{typeSupplyId}")
	public MicroserviceTypeSupplyDto findTypeSuppleById(@PathVariable Long typeSupplyId);
	
	@PostMapping("/api/providers-supplies/v1/{providerId}/type-supplies")
	public MicroserviceTypeSupplyDto createTypeSupplies(@PathVariable Long providerId);

	@GetMapping("/api/providers-supplies/v1/providers/{providerId}/users")
	public List<MicroserviceProviderUserDto> findUsersByProviderIdAndProfiles(@PathVariable Long providerId,
			@RequestParam(name = "profiles", required = false) List<Long> profiles) throws BusinessException;

	@GetMapping("/api/providers-supplies/v1/requests/emmiters")
	public List<MicroserviceRequestDto> findRequestsByEmmiters(
			@RequestParam(name = "emmiter_code", required = true) Long emmiterCode,
			@RequestParam(name = "emmiter_type", required = true) String emmiterType);

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
