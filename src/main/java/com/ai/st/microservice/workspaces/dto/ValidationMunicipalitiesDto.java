package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ValidationMunicipalitiesDto")
public class ValidationMunicipalitiesDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = true, notes = "Municipality ID")
    private Long municipalityId;

    @ApiModelProperty(required = true, notes = "Municipality Name")
    private String municipalityName;

    @ApiModelProperty(required = true, notes = "Municipality Code (DIVIPOLA)")
    private String municipalityCode;

    @ApiModelProperty(required = true, notes = "Does municipality have a conflict?")
    private Boolean conflict;

    @ApiModelProperty(required = true, notes = "Does municipality have a conflict?")
    private List<WorkspaceManagerDto> managers;

    public ValidationMunicipalitiesDto() {
        this.managers = new ArrayList<>();
    }

    public ValidationMunicipalitiesDto(Long municipalityId, String municipalityName, String municipalityCode) {
        super();
        this.municipalityId = municipalityId;
        this.municipalityName = municipalityName;
        this.municipalityCode = municipalityCode;
        this.conflict = false;
        this.managers = new ArrayList<>();
    }

    public Long getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(Long municipalityId) {
        this.municipalityId = municipalityId;
    }

    public Boolean getConflict() {
        return conflict;
    }

    public void setConflict(Boolean conflict) {
        this.conflict = conflict;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public List<WorkspaceManagerDto> getManagers() {
        return managers;
    }

    public void setManagers(List<WorkspaceManagerDto> managers) {
        this.managers = managers;
    }

}
