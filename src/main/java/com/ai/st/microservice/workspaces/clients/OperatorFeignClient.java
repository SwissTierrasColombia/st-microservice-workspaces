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
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceUpdateDeliveredSupplyDto;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "st-microservice-operators", configuration = OperatorFeignClient.Configuration.class)
public interface OperatorFeignClient {

	@GetMapping("/api/operators/v1/operators/{operatorId}")
	public MicroserviceOperatorDto findById(@PathVariable Long operatorId);

	@RequestMapping(method = RequestMethod.POST, value = "/api/operators/v1/operators/{operatorId}/deliveries", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceDeliveryDto createDelivery(@PathVariable Long operatorId,
			@RequestBody MicroserviceCreateDeliveryDto data);

	@GetMapping("/api/operators/v1/operators/{operatorId}/deliveries")
	public List<MicroserviceDeliveryDto> findDeliveriesByOperator(@PathVariable Long operatorId,
			@RequestParam(name = "municipality", required = false) String municipalityCode);

	@GetMapping("api/operators/v1/users/{userCode}/operators")
	public MicroserviceOperatorDto findByUserCode(@PathVariable Long userCode);

	@GetMapping("/api/operators/v1/operators/{operatorId}/deliveries")
	public List<MicroserviceDeliveryDto> findDeliveriesActivesByOperator(@PathVariable Long operatorId,
			@RequestParam(name = "active", required = false) Boolean isActive);

	@RequestMapping(method = RequestMethod.PUT, value = "/api/operators/v1/deliveries/{deliveryId}/supplies/{supplyId}", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceDeliveryDto updateSupplyDelivered(@PathVariable Long deliveryId, @PathVariable Long supplyId,
			@RequestBody MicroserviceUpdateDeliveredSupplyDto updateSupply);

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
