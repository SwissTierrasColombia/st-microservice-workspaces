package com.ai.st.microservice.workspaces.dto.ili;

import java.io.Serializable;

public class MicroserviceIlivalidatorBackgroundDto implements Serializable {

    private static final long serialVersionUID = -5774043946431854011L;

    private String pathFile;
    private Long requestId;
    private Long supplyRequestedId;
    private Long userCode;
    private String observations;
    private String versionModel;
    private Boolean hasGeometryValidation;
    private Boolean skipErrors;
    private String referenceId;

    public MicroserviceIlivalidatorBackgroundDto() {

    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getSupplyRequestedId() {
        return supplyRequestedId;
    }

    public void setSupplyRequestedId(Long supplyRequestedId) {
        this.supplyRequestedId = supplyRequestedId;
    }

    public Long getUserCode() {
        return userCode;
    }

    public void setUserCode(Long userCode) {
        this.userCode = userCode;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getVersionModel() {
        return versionModel;
    }

    public void setVersionModel(String versionModel) {
        this.versionModel = versionModel;
    }

    public Boolean getHasGeometryValidation() {
        return hasGeometryValidation;
    }

    public void setHasGeometryValidation(Boolean hasGeometryValidation) {
        this.hasGeometryValidation = hasGeometryValidation;
    }

    public Boolean getSkipErrors() {
        return skipErrors;
    }

    public void setSkipErrors(Boolean skipErrors) {
        this.skipErrors = skipErrors;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
