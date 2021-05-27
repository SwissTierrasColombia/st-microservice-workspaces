package com.ai.st.microservice.workspaces.clients;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.ai.st.microservice.common.clients.UserFeignClient;
import com.ai.st.microservice.common.dto.administration.MicroserviceChangePasswordDto;
import com.ai.st.microservice.common.dto.administration.MicroserviceCreateUserDto;
import com.ai.st.microservice.common.dto.administration.MicroserviceUpdateUserDto;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.administration.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "st-microservice-administration", configuration = UserFeignClient.Configuration.class)
public interface AdministrationFeignClient {

    @GetMapping("/api/administration/v1/users/{id}")
    WorkspaceUserDto findById(
            @PathVariable("id") Long id);

    @PostMapping(value = "/api/administration/v1/users", consumes = APPLICATION_JSON_VALUE)
    WorkspaceUserDto createUser(
            @RequestBody MicroserviceCreateUserDto user)
            throws BusinessException;

    @PostMapping(value = "/api/administration/v1/users/{id}/reset-password", consumes = APPLICATION_JSON_VALUE)
    WorkspaceUserDto changeUserPassword(
            @PathVariable(name = "id") Long userId,
            @RequestBody MicroserviceChangePasswordDto requestChangePassword)
            throws BusinessException;

    @PutMapping(value = "/api/administration/v1/users/{id}", consumes = APPLICATION_JSON_VALUE)
    WorkspaceUserDto updateUser(
            @PathVariable(name = "id") Long id,
            @RequestBody MicroserviceUpdateUserDto updateUser)
            throws BusinessException;

    @PutMapping(value = "/api/administration/v1/users/{userId}/disable", consumes = APPLICATION_JSON_VALUE)
    WorkspaceUserDto disableUser(
            @PathVariable(name = "userId") Long userId)
            throws BusinessException;

    @PutMapping(value = "/api/administration/v1/users/{userId}/enable", consumes = APPLICATION_JSON_VALUE)
    WorkspaceUserDto enableUser(
            @PathVariable(name = "userId") Long userId)
            throws BusinessException;

    @GetMapping("/api/administration/v1/users")
    List<WorkspaceUserDto> findUsersByRoles(
            @RequestParam(name = "roles", required = false) List<Long> roles);

}
