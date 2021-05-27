package com.ai.st.microservice.workspaces.dto.tasks;

import com.ai.st.microservice.common.dto.tasks.MicroserviceTaskCategoryDto;
import com.ai.st.microservice.common.dto.tasks.MicroserviceTaskMetadataDto;
import com.ai.st.microservice.common.dto.tasks.MicroserviceTaskStateDto;
import com.ai.st.microservice.common.dto.tasks.MicroserviceTaskStepDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MicroserviceTaskDto implements Serializable {

    private static final long serialVersionUID = -572797587427843121L;

    private Long id;
    private String name;
    private String description;
    private String reason;
    private Date deadline;
    private Date createdAt;
    private Date closingDate;
    private MicroserviceTaskStateDto taskState;
    private List<MicroserviceTaskMemberDto> members;
    private List<MicroserviceTaskCategoryDto> categories;
    private List<MicroserviceTaskMetadataDto> metadata;
    private List<MicroserviceTaskStepDto> steps;

    private Map<String, Object> data;

    public MicroserviceTaskDto() {
        this.members = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.metadata = new ArrayList<>();
        this.steps = new ArrayList<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(Date closingDate) {
        this.closingDate = closingDate;
    }

    public MicroserviceTaskStateDto getTaskState() {
        return taskState;
    }

    public void setTaskState(MicroserviceTaskStateDto taskState) {
        this.taskState = taskState;
    }

    public List<MicroserviceTaskMemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<MicroserviceTaskMemberDto> members) {
        this.members = members;
    }

    public List<MicroserviceTaskCategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<MicroserviceTaskCategoryDto> categories) {
        this.categories = categories;
    }

    public List<MicroserviceTaskMetadataDto> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MicroserviceTaskMetadataDto> metadata) {
        this.metadata = metadata;
    }

    public List<MicroserviceTaskStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<MicroserviceTaskStepDto> steps) {
        this.steps = steps;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
