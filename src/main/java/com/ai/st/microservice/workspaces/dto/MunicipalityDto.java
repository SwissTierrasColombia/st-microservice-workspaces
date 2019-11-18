package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MunicipalityDto", description = "Municipality Dto")
public class MunicipalityDto implements Serializable {

	private static final long serialVersionUID = 5342779101170022387L;

	@ApiModelProperty(required = true, notes = "Municipality ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Municipality name")
	private String name;

	@ApiModelProperty(required = true, notes = "Municipality code")
	private String code;

	@ApiModelProperty(required = true, notes = "Department")
	private DepartmentDto department;

	public MunicipalityDto() {

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public DepartmentDto getDepartment() {
		return department;
	}

	public void setDepartment(DepartmentDto department) {
		this.department = department;
	}

}
