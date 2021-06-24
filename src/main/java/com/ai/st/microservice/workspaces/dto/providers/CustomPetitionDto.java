package com.ai.st.microservice.workspaces.dto.providers;

import com.ai.st.microservice.common.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.common.dto.providers.MicroservicePetitionDto;

import java.io.Serializable;

public final class CustomPetitionDto extends MicroservicePetitionDto implements Serializable {

    private MicroserviceManagerDto manager;

    public CustomPetitionDto() {
        super();
    }

    public CustomPetitionDto(MicroservicePetitionDto response) {
        super();
        this.setId(response.getId());
        this.setCreatedAt(response.getCreatedAt());
        this.setManagerCode(response.getManagerCode());
        this.setObservations(response.getObservations());
        this.setJustification(response.getJustification());
        this.setPetitionState(response.getPetitionState());
        this.setProvider(response.getProvider());
    }

    public MicroserviceManagerDto getManager() {
        return manager;
    }

    public void setManager(MicroserviceManagerDto manager) {
        this.manager = manager;
    }

}
