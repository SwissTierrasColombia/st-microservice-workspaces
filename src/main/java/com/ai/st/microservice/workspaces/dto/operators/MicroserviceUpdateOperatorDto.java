package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "UpdateOperatorDto", description = "Update Operator Dto")
public class MicroserviceUpdateOperatorDto implements Serializable {

	private static final long serialVersionUID = 4784320463657739097L;

	@ApiModelProperty(required = true, notes = "Operator ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Operator name")
	private String name;

	@ApiModelProperty(required = true, notes = "Operator tax identification number")
	private String taxIdentificationNumber;

	@ApiModelProperty(required = true, notes = "Is public ?")
	private Boolean isPublic;

	@ApiModelProperty(required = true, notes = "State")
	private MicroserviceOperatorStateDto operatorState;

	public MicroserviceUpdateOperatorDto() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTaxIdentificationNumber() {
		return taxIdentificationNumber;
	}

	public void setTaxIdentificationNumber(String taxIdentificationNumber) {
		this.taxIdentificationNumber = taxIdentificationNumber;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public MicroserviceOperatorStateDto getOperatorState() {
		return operatorState;
	}

	public void setOperatorState(MicroserviceOperatorStateDto operatorState) {
		this.operatorState = operatorState;
	}

}
