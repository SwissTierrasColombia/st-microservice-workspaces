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

import com.ai.st.microservice.workspaces.dto.administration.MicroserviceChangePasswordDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceCreateUserDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUpdateUserDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "st-microservice-administration", configuration = UserFeignClient.Configuration.class)
public interface UserFeignClient {

	@GetMapping("/api/administration/v1/users/{id}")
	public MicroserviceUserDto findById(@PathVariable Long id);

	@GetMapping("/api/administration/v1/users/token")
	public MicroserviceUserDto findByToken(@RequestParam(name = "token") String token);

	@RequestMapping(method = RequestMethod.POST, value = "/api/administration/v1/users", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceUserDto createUser(@RequestBody MicroserviceCreateUserDto user) throws BusinessException;

	@RequestMapping(method = RequestMethod.POST, value = "/api/administration/v1/users/{id}/reset-password", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceUserDto changeUserPassword(@PathVariable(required = true, name = "id") Long userId,
			@RequestBody MicroserviceChangePasswordDto requestChangePassword) throws BusinessException;

	@RequestMapping(method = RequestMethod.PUT, value = "/api/administration/v1/users/{id}", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceUserDto updateUser(@PathVariable Long id, @RequestBody MicroserviceUpdateUserDto updateUser)
			throws BusinessException;

	@RequestMapping(method = RequestMethod.PUT, value = "/api/administration/v1/users/{userId}/disable", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceUserDto disableUser(@PathVariable Long userId) throws BusinessException;

	@RequestMapping(method = RequestMethod.PUT, value = "/api/administration/v1/users/{userId}/enable", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceUserDto enableUser(@PathVariable Long userId) throws BusinessException;

	@GetMapping("/api/administration/v1/users")
	public List<MicroserviceUserDto> findUsersByRoles(@RequestParam(name = "roles", required = false) List<Long> roles);

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
