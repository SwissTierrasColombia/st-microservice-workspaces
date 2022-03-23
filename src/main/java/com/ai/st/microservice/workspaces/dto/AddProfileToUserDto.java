package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "AddProfileToUserDto", description = "Add Profile To User Dto")
public class AddProfileToUserDto implements Serializable {

    private static final long serialVersionUID = 9169071345697135149L;

    @ApiModelProperty(required = true, notes = "Profile ID")
    private Long profileId;

    public AddProfileToUserDto() {

    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

}
