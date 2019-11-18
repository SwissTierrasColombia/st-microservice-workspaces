package com.ai.st.microservice.workspaces.exceptions;

public class InputValidationException extends Exception {

	private static final long serialVersionUID = 6631825550389136362L;

	private String messageError;

	public InputValidationException(String message) {
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
