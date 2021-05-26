package com.ai.st.microservice.workspaces.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceRoleDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;

@Component
public class UserBusiness {

    private final Logger log = LoggerFactory.getLogger(UserBusiness.class);

    private final UserFeignClient userClient;

    public UserBusiness(UserFeignClient userClient) {
        this.userClient = userClient;
    }

    public MicroserviceUserDto getUserById(Long userId) {
        MicroserviceUserDto userDto = null;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.info("Error consultando el usuario: " + e.getMessage());
        }
        return userDto;
    }

    public MicroserviceUserDto getUserByToken(String headerAuthorization) {
        MicroserviceUserDto userDto = null;
        try {
            String token = headerAuthorization.replace("Bearer ", "").trim();
            userDto = userClient.findByToken(token);
        } catch (Exception e) {
            log.info("Error consultando el usuario: " + e.getMessage());
        }
        return userDto;
    }

    public boolean isManager(MicroserviceUserDto userDto) {
        MicroserviceRoleDto roleManager = userDto.getRoles().stream()
                .filter(roleDto -> roleDto.getId().equals(RoleBusiness.ROLE_MANAGER)).findAny().orElse(null);

        return roleManager != null;
    }

    public boolean isSuperAdministrator(MicroserviceUserDto userDto) {

        MicroserviceRoleDto roleSuper = userDto.getRoles().stream()
                .filter(roleDto -> roleDto.getId().equals(RoleBusiness.ROLE_SUPER_ADMINISTRATOR)).findAny()
                .orElse(null);

        return roleSuper != null;
    }

    public boolean isAdministrator(MicroserviceUserDto userDto) {

        MicroserviceRoleDto roleAdministrator = userDto.getRoles().stream()
                .filter(roleDto -> roleDto.getId().equals(RoleBusiness.ROLE_ADMINISTRATOR)).findAny().orElse(null);

        return roleAdministrator != null;
    }

    public boolean isOperator(MicroserviceUserDto userDto) {

        MicroserviceRoleDto roleOperator = userDto.getRoles().stream()
                .filter(roleDto -> roleDto.getId().equals(RoleBusiness.ROLE_OPERATOR)).findAny().orElse(null);

        return roleOperator != null;
    }

    public boolean isProvider(MicroserviceUserDto userDto) {

        MicroserviceRoleDto roleProvider = userDto.getRoles().stream()
                .filter(roleDto -> roleDto.getId().equals(RoleBusiness.ROLE_SUPPLY_SUPPLIER)).findAny()
                .orElse(null);

        return roleProvider != null;
    }

}
