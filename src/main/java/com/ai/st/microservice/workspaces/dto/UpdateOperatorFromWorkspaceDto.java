package com.ai.st.microservice.workspaces.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@ApiModel(value = "UpdateOperatorFromWorkspaceDto")
public class UpdateOperatorFromWorkspaceDto implements Serializable {

    @ApiModelProperty(required = true, notes = "Start date")
    private String startDate;

    @ApiModelProperty(required = true, notes = "End date")
    private String endDate;

    @ApiModelProperty(required = true, notes = "Observations")
    private String observations;

    @ApiModelProperty(required = true, notes = "Support file")
    private MultipartFile supportFile;

    @ApiModelProperty(required = true, notes = "Number parcels expected")
    private Long numberParcelsExpected;

    @ApiModelProperty(required = true, notes = "Work area")
    private Double workArea;

    public UpdateOperatorFromWorkspaceDto() {

    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public MultipartFile getSupportFile() {
        return supportFile;
    }

    public void setSupportFile(MultipartFile supportFile) {
        this.supportFile = supportFile;
    }

    public Long getNumberParcelsExpected() {
        return numberParcelsExpected;
    }

    public void setNumberParcelsExpected(Long numberParcelsExpected) {
        this.numberParcelsExpected = numberParcelsExpected;
    }

    public Double getWorkArea() {
        return workArea;
    }

    public void setWorkArea(Double workArea) {
        this.workArea = workArea;
    }
}
