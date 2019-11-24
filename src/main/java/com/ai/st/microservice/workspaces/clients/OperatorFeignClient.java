package com.ai.st.microservice.workspaces.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ai.st.microservice.workspaces.dto.operators.OperatorDto;

@FeignClient(name = "st-microservice-operators")
public interface OperatorFeignClient {

	@GetMapping("/api/operators/v1/operators/{operatorId}")
	public OperatorDto findById(@PathVariable Long operatorId);

}
