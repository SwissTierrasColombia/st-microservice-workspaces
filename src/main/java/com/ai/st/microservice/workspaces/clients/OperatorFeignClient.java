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

import com.ai.st.microservice.workspaces.dto.operators.MicroserviceAddUserToOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorUserDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceUpdateDeliveredSupplyDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceUpdateDeliveryDto;

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

	@RequestMapping(method = RequestMethod.PUT, value = "/api/operators/v1/deliveries/{deliveryId}/disable", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceDeliveryDto disableDelivery(@PathVariable Long deliveryId);

	@GetMapping("/api/operators/v1/deliveries/{deliveryId}")
	public MicroserviceDeliveryDto findDeliveryById(@PathVariable Long deliveryId);

	@RequestMapping(method = RequestMethod.POST, value = "/api/operators/v1/users", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceOperatorDto addUserToOperator(@RequestBody MicroserviceAddUserToOperatorDto requestAddUser);

	@GetMapping("/api/operators/v1/operators/{operatorId}/users")
	public List<MicroserviceOperatorUserDto> getUsersByOperator(@PathVariable Long operatorId);

	@RequestMapping(method = RequestMethod.PUT, value = "/api/operators/v1/deliveries/{deliveryId}", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceDeliveryDto updateDelivery(@PathVariable Long deliveryId,
			@RequestBody MicroserviceUpdateDeliveryDto data);

	@GetMapping("/api/operators/v1/operators/{operatorId}/deliveries")
	public List<MicroserviceDeliveryDto> findDeliveriesByOperator(@PathVariable Long operatorId,
			@RequestParam(name = "municipality", required = false) String municipalityCode,
			@RequestParam(name = "active", required = false) Boolean active);

	@GetMapping("/api/operators/v1/deliveries/managers/{managerId}")
	public List<MicroserviceDeliveryDto> findDeliveriesByManager(@PathVariable Long managerId);

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
