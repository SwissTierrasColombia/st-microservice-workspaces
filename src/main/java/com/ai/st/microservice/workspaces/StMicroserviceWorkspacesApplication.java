package com.ai.st.microservice.workspaces;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import com.ai.st.microservice.workspaces.clients.MyErrorDecoder;

@EnableFeignClients
@SpringBootApplication
@EnableEurekaClient
public class StMicroserviceWorkspacesApplication {

	public static void main(String[] args) {
		SpringApplication.run(StMicroserviceWorkspacesApplication.class, args);
	}
	
	@Bean
	public MyErrorDecoder myErrorDecoder() {
	  return new MyErrorDecoder();
	}

}
