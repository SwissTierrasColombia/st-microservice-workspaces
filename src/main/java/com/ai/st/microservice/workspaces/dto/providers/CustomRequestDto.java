package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.dto.providers.*;

import com.ai.st.microservice.workspaces.dto.MunicipalityDto;

public class CustomRequestDto extends MicroserviceRequestDto implements Serializable {

    private MunicipalityDto municipality;
    private MicroserviceUserDto userClosedBy;

    public CustomRequestDto() {
        super();
    }

    public CustomRequestDto(MicroserviceRequestDto response) {
        super();
        this.setId(response.getId());
        this.setCreatedAt(response.getCreatedAt());
        this.setDeadline(response.getDeadline());
        this.setObservations(response.getObservations());
        this.setRequestState(response.getRequestState());
        this.setSuppliesRequested(response.getSuppliesRequested());
        this.setEmitters(response.getEmitters());
        this.setProvider(response.getProvider());
        this.setMunicipalityCode(response.getMunicipalityCode());
        this.setPackageLabel(response.getPackageLabel());
        this.setClosedAt(response.getClosedAt());
        this.setClosedBy(response.getClosedBy());
    }

    public MunicipalityDto getMunicipality() {
        return municipality;
    }

    public void setMunicipality(MunicipalityDto municipality) {
        this.municipality = municipality;
    }

    public MicroserviceUserDto getUserClosedBy() {
        return userClosedBy;
    }

    public void setUserClosedBy(MicroserviceUserDto userClosedBy) {
        this.userClosedBy = userClosedBy;
    }

}
