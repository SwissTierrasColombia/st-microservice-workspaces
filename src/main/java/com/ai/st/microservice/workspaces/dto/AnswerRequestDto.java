package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "AnswerRequestDto")
public class AnswerRequestDto implements Serializable {

    private static final long serialVersionUID = 177256820188869027L;

    @ApiModelProperty(required = true, notes = "Type Supply")
    private Long typeSupplyId;

    @ApiModelProperty(notes = "Justification")
    private String justification;

    @ApiModelProperty(notes = "Url")
    private String url;

    @ApiModelProperty(notes = "Observations")
    private String observations;

    @ApiModelProperty(notes = "Skip errors?")
    private Boolean skipErrors;

    public AnswerRequestDto() {
        this.skipErrors = true;
    }

    public Long getTypeSupplyId() {
        return typeSupplyId;
    }

    public void setTypeSupplyId(Long typeSupplyId) {
        this.typeSupplyId = typeSupplyId;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
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

    public Boolean getSkipErrors() {
        return skipErrors;
    }

    public void setSkipErrors(Boolean skipErrors) {
        this.skipErrors = skipErrors;
    }
}
