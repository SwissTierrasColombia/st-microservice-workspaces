package com.ai.st.microservice.workspaces.dto.administration;

import java.io.Serializable;

public class MicroserviceUpdateUserDto implements Serializable {

    private static final long serialVersionUID = -2041172889834251468L;

    private String firstName;
    private String lastName;
    private String email;

    public MicroserviceUpdateUserDto() {

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
