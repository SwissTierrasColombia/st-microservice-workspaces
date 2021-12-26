package com.ai.st.microservice.workspaces.dto.administration;

import com.ai.st.microservice.common.dto.administration.MicroserviceRoleDto;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceProviderRoleDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class CustomUserDto extends MicroserviceUserDto {

    private List<MicroserviceManagerProfileDto> profilesManager;
    private List<MicroserviceProviderProfileDto> profilesProvider;
    private List<MicroserviceProviderRoleDto> rolesProvider;
    private Object entity;

    public CustomUserDto() {
        super();
        this.profilesManager = new ArrayList<>();
        this.profilesProvider = new ArrayList<>();
        this.rolesProvider = new ArrayList<>();
    }

    public CustomUserDto(Long id, String firstName, String lastName, String email, String username, String password,
                         Boolean enabled, Date createdAt, Date updatedAt, List<MicroserviceRoleDto> roles) {
        super();
        this.setId(id);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setUsername(username);
        this.setPassword(password);
        this.setEnabled(enabled);
        this.setCreatedAt(createdAt);
        this.setUpdatedAt(updatedAt);
        this.setRoles(roles);
        this.profilesManager = new ArrayList<>();
        this.profilesProvider = new ArrayList<>();
        this.rolesProvider = new ArrayList<>();
    }

    public CustomUserDto(MicroserviceUserDto response) {
        this.setId(response.getId());
        this.setFirstName(response.getFirstName());
        this.setLastName(response.getLastName());
        this.setEmail(response.getEmail());
        this.setUsername(response.getUsername());
        this.setPassword(response.getPassword());
        this.setEnabled(response.getEnabled());
        this.setCreatedAt(response.getCreatedAt());
        this.setUpdatedAt(response.getUpdatedAt());
        this.setRoles(response.getRoles());
        this.profilesManager = new ArrayList<>();
        this.profilesProvider = new ArrayList<>();
        this.rolesProvider = new ArrayList<>();
    }

    public List<MicroserviceManagerProfileDto> getProfilesManager() {
        return profilesManager;
    }

    public void setProfilesManager(List<MicroserviceManagerProfileDto> profilesManager) {
        this.profilesManager = profilesManager;
    }

    public List<MicroserviceProviderProfileDto> getProfilesProvider() {
        return profilesProvider;
    }

    public void setProfilesProvider(List<MicroserviceProviderProfileDto> profilesProvider) {
        this.profilesProvider = profilesProvider;
    }

    public List<MicroserviceProviderRoleDto> getRolesProvider() {
        return rolesProvider;
    }

    public void setRolesProvider(List<MicroserviceProviderRoleDto> rolesProvider) {
        this.rolesProvider = rolesProvider;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

}
