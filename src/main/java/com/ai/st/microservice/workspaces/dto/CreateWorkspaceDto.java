package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreateWorkspaceDto", description = "Create Workspace Dto")
public class CreateWorkspaceDto implements Serializable {

	private static final long serialVersionUID = -6853674829966701765L;

	@ApiModelProperty(required = true, notes = "Start date")
	private String startDate;

	@ApiModelProperty(required = true, notes = "Manager Code")
	private Long managerCode;

	@ApiModelProperty(required = true, notes = "Observations")
	private String observations;

	@ApiModelProperty(required = true, notes = "Municipalities")
	private List<Long> municipalities;

	@ApiModelProperty(required = true, notes = "Support file")
	private MultipartFile supportFile;

	public CreateWorkspaceDto() {
		this.municipalities = new ArrayList<Long>();
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public Long getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(Long managerCode) {
		this.managerCode = managerCode;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public MultipartFile getSupportFile() {
		return supportFile;
	}

	public void setSupportFile(MultipartFile supportFile) {
		this.supportFile = supportFile;
	}

	public List<Long> getMunicipalities() {
		return municipalities;
	}

	public void setMunicipalities(List<Long> municipalities) {
		this.municipalities = municipalities;
	}

}
