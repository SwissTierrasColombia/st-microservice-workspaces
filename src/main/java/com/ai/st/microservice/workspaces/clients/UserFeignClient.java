package com.ai.st.microservice.workspaces.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.st.microservice.workspaces.dto.administration.UserDto;

@FeignClient(name = "st-microservice-administration")
public interface UserFeignClient {

	@GetMapping("/api/administration/users/{id}")
	public UserDto findById(@PathVariable Long id);

	@GetMapping("/api/administration/users/token")
	public UserDto findByToken(@RequestParam(name = "token") String token);

}
