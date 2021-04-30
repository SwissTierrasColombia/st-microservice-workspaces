package com.ai.st.microservice.workspaces.dto.providers;

import java.io.Serializable;

public class MicroserviceUpdateSupplyRequestedDto implements Serializable {

    private static final long serialVersionUID = -1302858811309199472L;

    private Boolean delivered;
    private String justification;
    private Long supplyRequestedStateId;
    private Long deliveryBy;
    private String url;
    private String observations;
    private String errors;
    private String ftp;
    private Boolean validated;
    private String log;
    private String extraFile;

    public Boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public Long getSupplyRequestedStateId() {
        return supplyRequestedStateId;
    }

    public void setSupplyRequestedStateId(Long supplyRequestedStateId) {
        this.supplyRequestedStateId = supplyRequestedStateId;
    }

    public Long getDeliveryBy() {
        return deliveryBy;
    }

    public void setDeliveryBy(Long deliveryBy) {
        this.deliveryBy = deliveryBy;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getFtp() {
        return ftp;
    }

    public void setFtp(String ftp) {
        this.ftp = ftp;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public Boolean isValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getExtraFile() {
        return extraFile;
    }

    public void setExtraFile(String extraFile) {
        this.extraFile = extraFile;
    }
}
