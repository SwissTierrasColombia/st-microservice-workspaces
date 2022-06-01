package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "AssignOperatorWorkspaceDto")
public class AssignOperatorWorkspaceDto implements Serializable {

    private static final long serialVersionUID = -4217826518362864745L;

    @ApiModelProperty(required = true, notes = "Start date")
    private String startDate;

    @ApiModelProperty(required = true, notes = "End date")
    private String endDate;

    @ApiModelProperty(required = true, notes = "Operator Code")
    private Long operatorCode;

    @ApiModelProperty(required = false, notes = "Observations")
    private String observations;

    @ApiModelProperty(required = true, notes = "Support file")
    private MultipartFile supportFile;

    @ApiModelProperty(required = false, notes = "Number parcels expected")
    private Long numberParcelsExpected;

    @ApiModelProperty(required = false, notes = "Work area")
    private Double workArea;

    public AssignOperatorWorkspaceDto() {

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

    public Long getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(Long operatorCode) {
        this.operatorCode = operatorCode;
    }

    public MultipartFile getSupportFile() {
        return supportFile;
    }

    public void setSupportFile(MultipartFile supportFile) {
        this.supportFile = supportFile;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
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

    @Override
    public String toString() {
        return "AssignOperatorWorkspaceDto{" + "startDate='" + startDate + '\'' + ", endDate='" + endDate + '\''
                + ", operatorCode=" + operatorCode + ", observations='" + observations + '\''
                + ", numberParcelsExpected=" + numberParcelsExpected + ", workArea=" + workArea + '}';
    }
}
