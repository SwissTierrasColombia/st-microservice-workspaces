package com.ai.st.microservice.workspaces.dto.ili;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceIntegrationStatDto implements Serializable {

    private static final long serialVersionUID = 6963849125361470575L;

    private long countSNR;
    private long countGC;
    private long countMatch;
    private double percentage;
    private boolean status;
    private Long integrationId;
    private List<String> errors;

    public MicroserviceIntegrationStatDto() {
        this.countGC = 0;
        this.countSNR = 0;
        this.countMatch = 0;
        this.percentage = 0.0;
        this.errors = new ArrayList<>();
    }

    public long getCountSNR() {
        return countSNR;
    }

    public void setCountSNR(long countSNR) {
        this.countSNR = countSNR;
    }

    public long getCountGC() {
        return countGC;
    }

    public void setCountGC(long countGC) {
        this.countGC = countGC;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public long getCountMatch() {
        return countMatch;
    }

    public void setCountMatch(long countMatch) {
        this.countMatch = countMatch;
    }

    public Long getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(Long integrationId) {
        this.integrationId = integrationId;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
