package com.ai.st.microservice.workspaces.clients;

import java.io.IOException;

import org.springframework.http.HttpStatus;

import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import feign.codec.ErrorDecoder;

public class MyErrorDecoder implements ErrorDecoder {

	private final ErrorDecoder defaultErrorDecoder = new Default();

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public Exception decode(String methodKey, Response response) {

		if (response.body() == null) {
			return defaultErrorDecoder.decode(methodKey, response);
		}

		BasicResponseDto error;
		try {
			error = mapper.readValue(response.body().asInputStream(), BasicResponseDto.class);
		} catch (IOException e) {
			return defaultErrorDecoder.decode(methodKey, response);
		}

		final HttpStatus status = HttpStatus.valueOf(response.status());

		if (status.is4xxClientError()) {
			return new BusinessException(error.getMessage());
		} else {
			return new BusinessException("Error");
		}
	}

}