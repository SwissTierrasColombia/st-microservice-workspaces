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

import com.ai.st.microservice.workspaces.dto.managers.MicroserviceAddUserToManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceCreateManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerUserDto;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "st-microservice-managers", configuration = ManagerFeignClient.Configuration.class)
public interface ManagerFeignClient {

	@GetMapping("/api/managers/v1/managers/{managerId}")
	public MicroserviceManagerDto findById(@PathVariable Long managerId);

	@GetMapping("/api/managers/v1/users/{userCode}/managers")
	public MicroserviceManagerDto findByUserCode(@PathVariable Long userCode);

	@RequestMapping(method = RequestMethod.POST, value = "/api/managers/v1/users", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceManagerUserDto addUserToManager(@RequestBody MicroserviceAddUserToManagerDto data);
	
	@GetMapping("/api/managers/v1/users/{userCode}/profiles")
	public List<MicroserviceManagerProfileDto> findProfilesByUser(@PathVariable Long userCode);
	
	@GetMapping("/api/managers/v1/managers/{managerId}/users")
	public List<MicroserviceManagerUserDto> findUsersByManager(@PathVariable Long managerId,
			@RequestParam(required = false, name = "profiles") List<Long> profiles);
	
	@RequestMapping(method = RequestMethod.POST, value = "/api/managers/v1/managers", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceManagerDto addManager(@RequestBody MicroserviceCreateManagerDto data);
	
	@RequestMapping(method = RequestMethod.PUT, value = "/api/managers/v1/managers/{id}/activate", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceManagerDto activateManager(@PathVariable Long id);
	
	@RequestMapping(method = RequestMethod.PUT, value = "/api/managers/v1/managers/{id}/deactivate", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceManagerDto deactivateManager(@PathVariable Long id);

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
