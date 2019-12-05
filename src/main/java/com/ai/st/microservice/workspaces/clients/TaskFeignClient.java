package com.ai.st.microservice.workspaces.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskDto;

@FeignClient(name = "st-microservice-tasks")
public interface TaskFeignClient {

	@GetMapping("/api/tasks/v1/tasks")
	public List<MicroserviceTaskDto> findByUserAndState(
			@RequestParam(required = false, name = "member") Long memberCode,
			@RequestParam(required = false, name = "state") Long taskStateId);

}
