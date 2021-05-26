package com.ai.st.microservice.workspaces.exceptions;

public class DisconnectedMicroserviceException extends Exception {

    private static final long serialVersionUID = 3230476461126206019L;

    private final String messageError;

    public DisconnectedMicroserviceException(String message) {
        super();
        this.messageError = message;
    }

    public String getMessageError() {
        return messageError;
    }

    @Override
    public String getMessage() {
        return this.getMessageError();
    }

}
