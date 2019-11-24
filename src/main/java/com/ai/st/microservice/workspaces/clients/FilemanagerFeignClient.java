package com.ai.st.microservice.workspaces.clients;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ai.st.microservice.workspaces.dto.managers.ManagerDto;

@FeignClient(name = "st-microservice-filemanager", configuration = { FeignSupportConfig.class })
public interface FilemanagerFeignClient {

	@RequestMapping(value = "/api/filemanager/v1/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ManagerDto saveFile(@RequestParam(value = "file", name = "file") MultipartFile file,
			@RequestParam("driver") String driver);

}
