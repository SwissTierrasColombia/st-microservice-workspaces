package com.ai.st.microservice.workspaces.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "CreateSupplyCadastralAuthorityDto")
public class CreateSupplyCadastralAuthorityDto implements Serializable {

    private static final long serialVersionUID = 6161636556899381195L;

    @ApiModelProperty(required = true, notes = "Attachment Type ID")
    private Long attachmentTypeId;

    @ApiModelProperty(notes = "FTP")
    private String ftp;

    @ApiModelProperty(required = true, notes = "Observations")
    private String observations;

    @ApiModelProperty(required = true, notes = "Name")
    private String name;

    @ApiModelProperty(required = true, notes = "Manager")
    private Long managerCode;

    public CreateSupplyCadastralAuthorityDto() {

    }

    public Long getAttachmentTypeId() {
        return attachmentTypeId;
    }

    public void setAttachmentTypeId(Long attachmentTypeId) {
        this.attachmentTypeId = attachmentTypeId;
    }

    public String getFtp() {
        return ftp;
    }

    public void setFtp(String ftp) {
        this.ftp = ftp;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getManagerCode() {
        return managerCode;
    }

    public void setManagerCode(Long managerCode) {
        this.managerCode = managerCode;
    }

    @Override
    public String toString() {
        return "CreateSupplyCadastralAuthorityDto{" + "attachmentTypeId=" + attachmentTypeId + ", ftp='" + ftp + '\''
                + ", observations='" + observations + '\'' + ", name='" + name + '\'' + ", managerCode=" + managerCode
                + '}';
    }
}
