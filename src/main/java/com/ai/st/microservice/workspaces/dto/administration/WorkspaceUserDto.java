package com.ai.st.microservice.workspaces.dto.administration;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.common.dto.providers.MicroserviceProviderRoleDto;

import java.util.ArrayList;
import java.util.List;

public final class WorkspaceUserDto extends MicroserviceUserDto {

    private List<MicroserviceManagerProfileDto> profilesManager;
    private List<MicroserviceProviderProfileDto> profilesProvider;
    private List<MicroserviceProviderRoleDto> rolesProvider;
    private Object entity;

    public WorkspaceUserDto() {
        super();
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
