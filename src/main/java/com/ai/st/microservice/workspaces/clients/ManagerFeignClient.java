package com.ai.st.microservice.workspaces.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ai.st.microservice.workspaces.dto.managers.ManagerDto;

@FeignClient(name = "st-microservice-managers")
public interface ManagerFeignClient {

	@GetMapping("/api/managers/v1/managers/{managerId}")
	public ManagerDto findById(@PathVariable Long managerId);

	@GetMapping("/api/managers/v1/users/{userCode}/managers")
	public ManagerDto findByUserCode(@PathVariable Long userCode);

}
