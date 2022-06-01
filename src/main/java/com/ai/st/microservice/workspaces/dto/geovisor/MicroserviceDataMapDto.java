package com.ai.st.microservice.workspaces.dto.geovisor;

import java.io.Serializable;

public class MicroserviceDataMapDto implements Serializable {

    private static final long serialVersionUID = 7795987778570923080L;

    private String st_geocreatefastcontext;

    public MicroserviceDataMapDto() {

    }

    public String getSt_geocreatefastcontext() {
        return st_geocreatefastcontext;
    }

    public void setSt_geocreatefastcontext(String st_geocreatefastcontext) {
        this.st_geocreatefastcontext = st_geocreatefastcontext;
    }

}
