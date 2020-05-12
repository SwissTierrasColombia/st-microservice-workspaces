package com.ai.st.microservice.workspaces.dto.operators;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateOperatorDto", description = "Create Operator Dto")
public class MicroserviceCreateOperatorDto implements Serializable {

	private static final long serialVersionUID = 4784320463657739097L;

	@ApiModelProperty(required = true, notes = "Operator name")
	private String name;

	@ApiModelProperty(required = true, notes = "Operator tax identification number")
	private String taxIdentificationNumber;

	@ApiModelProperty(required = true, notes = "Date creation")
	private Date createdAt;

	@ApiModelProperty(required = true, notes = "Is public ?")
	private Boolean isPublic;

	@ApiModelProperty(required = true, notes = "State")
	private MicroserviceOperatorStateDto operatorState;

	public MicroserviceCreateOperatorDto() {

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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
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
