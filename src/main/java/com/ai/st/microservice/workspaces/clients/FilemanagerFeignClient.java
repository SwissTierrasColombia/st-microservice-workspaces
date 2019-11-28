package com.ai.st.microservice.workspaces.clients;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.st.microservice.workspaces.dto.filemanager.MicroserviceFilemanagerResponseDto;

@FeignClient(name = "st-microservice-filemanager", configuration = { FeignSupportConfig.class })
public interface FilemanagerFeignClient {

	@PostMapping(value = "/api/filemanager/v1/filebytes")
	public MicroserviceFilemanagerResponseDto saveFile(@RequestParam(value = "file", name = "file") byte[] file,
			@RequestParam("filename") String filename, @RequestParam("namespace") String namespace,
			@RequestParam("driver") String driver);

}
