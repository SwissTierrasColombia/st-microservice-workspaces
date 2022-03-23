package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WorkspaceDto", description = "Workspace Dto")
public class WorkspaceDto implements Serializable {

    private static final long serialVersionUID = 3626653639569669214L;

    @ApiModelProperty(required = true, notes = "Workspace ID")
    private Long id;

    @ApiModelProperty(required = true, notes = "Is active?")
    private Boolean isActive;

    @ApiModelProperty(required = true, notes = "Date creation")
    private Date createdAt;

    @ApiModelProperty(required = false, notes = "Date update")
    private Date updatedAt;

    @ApiModelProperty(required = false, notes = "Operators")
    private List<WorkspaceOperatorDto> operators;

    @ApiModelProperty(required = false, notes = "Managers")
    private List<WorkspaceManagerDto> managers;

    @ApiModelProperty(required = false, notes = "Municipality")
    private MunicipalityDto municipality;

    public WorkspaceDto() {
        operators = new ArrayList<WorkspaceOperatorDto>();
        managers = new ArrayList<WorkspaceManagerDto>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<WorkspaceOperatorDto> getOperators() {
        return operators;
    }

    public void setOperators(List<WorkspaceOperatorDto> operators) {
        this.operators = operators;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public MunicipalityDto getMunicipality() {
        return municipality;
    }

    public void setMunicipality(MunicipalityDto municipality) {
        this.municipality = municipality;
    }

    public List<WorkspaceManagerDto> getManagers() {
        return managers;
    }

    public void setManagers(List<WorkspaceManagerDto> managers) {
        this.managers = managers;
    }

}
