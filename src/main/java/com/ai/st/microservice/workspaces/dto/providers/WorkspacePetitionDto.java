package com.ai.st.microservice.workspaces.dto.providers;

import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.providers.MicroservicePetitionDto;

public final class WorkspacePetitionDto extends MicroservicePetitionDto {

    private MicroserviceManagerDto manager;

    public MicroserviceManagerDto getManager() {
        return manager;
    }

    public void setManager(MicroserviceManagerDto manager) {
        this.manager = manager;
    }

}
