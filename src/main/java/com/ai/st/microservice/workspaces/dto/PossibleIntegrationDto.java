package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "PossibleIntegrationDto")
public class PossibleIntegrationDto implements Serializable {

    private static final long serialVersionUID = 9161755714691932192L;

    @ApiModelProperty(required = true, notes = "Municipality")
    private MunicipalityDto municipality;

    public PossibleIntegrationDto() {

    }

    public PossibleIntegrationDto(MunicipalityDto municipality) {
        this.municipality = municipality;
    }

    public MunicipalityDto getMunicipality() {
        return municipality;
    }

    public void setMunicipality(MunicipalityDto municipality) {
        this.municipality = municipality;
    }

}
