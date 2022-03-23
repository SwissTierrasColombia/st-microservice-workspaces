package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "DepartmentDto", description = "Department Dto")
public class DepartmentDto implements Serializable {

    private static final long serialVersionUID = -4872570330522596800L;

    @ApiModelProperty(required = true, notes = "Department ID")
    private Long id;

    @ApiModelProperty(required = true, notes = "Department name")
    private String name;

    @ApiModelProperty(required = true, notes = "Department code")
    private String code;

    public DepartmentDto() {

    }

    public DepartmentDto(Long id, String name, String code) {
        super();
        this.id = id;
        this.name = name;
        this.code = code;
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

}
