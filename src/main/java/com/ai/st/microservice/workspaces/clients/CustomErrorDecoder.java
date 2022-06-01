package com.ai.st.microservice.workspaces.clients;

import com.ai.st.microservice.common.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.controllers.v1.IntegrationV1Controller;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomErrorDecoder implements ErrorDecoder {

    private final Logger log = LoggerFactory.getLogger(IntegrationV1Controller.class);

    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
        case 400:
            log.error("Error request (400): " + response.request().toString());
            log.error("Error request body (400): " + response.body().toString());
            return new BusinessException("Error 400 en la comunicación con el microservicio");
        case 500:
        default:
            log.error("Error request (500): " + response.request().toString());
            log.error("Error request body (500): " + response.body().toString());
            return new BusinessException("Error 500 en la comunicación con el microservicio");
        }

    }

}
