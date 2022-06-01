package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MakeIntegrationDto", description = "Make Integration Dto")
public class MakeIntegrationDto implements Serializable {

    private static final long serialVersionUID = -126620994344892312L;

    @ApiModelProperty(required = true, notes = "Supply ID (cadastre)")
    private Long supplyCadastre;

    @ApiModelProperty(required = true, notes = "Supply ID (registration)")
    private Long supplyRegistration;

    public MakeIntegrationDto() {

    }

    public Long getSupplyCadastre() {
        return supplyCadastre;
    }

    public void setSupplyCadastre(Long supplyCadastre) {
        this.supplyCadastre = supplyCadastre;
    }

    public Long getSupplyRegistration() {
        return supplyRegistration;
    }

    public void setSupplyRegistration(Long supplyRegistration) {
        this.supplyRegistration = supplyRegistration;
    }

    @Override
    public String toString() {
        return "MakeIntegrationDto{" + "supplyCadastre=" + supplyCadastre + ", supplyRegistration=" + supplyRegistration
                + '}';
    }
}
