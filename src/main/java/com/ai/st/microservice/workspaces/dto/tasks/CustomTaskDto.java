package com.ai.st.microservice.workspaces.dto.tasks;

import com.ai.st.microservice.common.dto.tasks.*;

import java.io.Serializable;
import java.util.Map;

public class CustomTaskDto extends MicroserviceTaskDto implements Serializable {

    private Map<String, Object> data;

    public CustomTaskDto() {
        super();
    }

    public CustomTaskDto(MicroserviceTaskDto response) {
        super();
        this.setId(response.getId());
        this.setName(response.getName());
        this.setDescription(response.getDescription());
        this.setReason(response.getReason());
        this.setDeadline(response.getDeadline());
        this.setCreatedAt(response.getCreatedAt());
        this.setClosingDate(response.getClosingDate());
        this.setTaskState(response.getTaskState());
        this.setMembers(response.getMembers());
        this.setCategories(response.getCategories());
        this.setMetadata(response.getMetadata());
        this.setSteps(response.getSteps());
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

}
