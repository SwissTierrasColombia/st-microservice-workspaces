package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceCreateSupplyDto implements Serializable {

    private static final long serialVersionUID = 7367459622173301312L;

    private String municipalityCode;
    private String observations;
    private String name;
    private Long typeSupplyCode;
    private Long requestCode;
    private String modelVersion;
    private Long supplyStateId;
    private Long managerCode;
    private List<MicroserviceCreateSupplyOwnerDto> owners;
    private List<MicroserviceCreateSupplyAttachmentDto> attachments;
    private Boolean hasGeometryValidation;

    public MicroserviceCreateSupplyDto() {
        this.owners = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Long getTypeSupplyCode() {
        return typeSupplyCode;
    }

    public void setTypeSupplyCode(Long typeSupplyCode) {
        this.typeSupplyCode = typeSupplyCode;
    }

    public List<MicroserviceCreateSupplyOwnerDto> getOwners() {
        return owners;
    }

    public void setOwners(List<MicroserviceCreateSupplyOwnerDto> owners) {
        this.owners = owners;
    }

    public Long getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(Long requestCode) {
        this.requestCode = requestCode;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public List<MicroserviceCreateSupplyAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MicroserviceCreateSupplyAttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public Long getSupplyStateId() {
        return supplyStateId;
    }

    public void setSupplyStateId(Long supplyStateId) {
        this.supplyStateId = supplyStateId;
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

    public Boolean getHasGeometryValidation() {
        return hasGeometryValidation;
    }

    public void setHasGeometryValidation(Boolean hasGeometryValidation) {
        this.hasGeometryValidation = hasGeometryValidation;
    }
}
