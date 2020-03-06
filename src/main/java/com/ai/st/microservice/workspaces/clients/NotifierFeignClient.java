package com.ai.st.microservice.workspaces.clients;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationAssignmentOperationMunicipalityDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationInputIntegrationsDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationInputRequestDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationLoadOfInputsDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationMunicipalityManagementDto;
import com.ai.st.microservice.workspaces.dto.notifications.MicroserviceNotificationNewUserDto;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "st-microservice-notifier", configuration = NotifierFeignClient.Configuration.class)
public interface NotifierFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "/api/notifier/v1/notify/new_user", consumes = APPLICATION_JSON_VALUE)
	public void creationUser(@RequestBody MicroserviceNotificationNewUserDto data);

	@RequestMapping(method = RequestMethod.POST, value = "/api/notifier/v1/notify/municipality_management_assignment", consumes = APPLICATION_JSON_VALUE)
	public void municipalityManagement(@RequestBody MicroserviceNotificationMunicipalityManagementDto data);

	@RequestMapping(method = RequestMethod.POST, value = "/api/notifier/v1/notify/assignment_operation_municipality", consumes = APPLICATION_JSON_VALUE)
	public void assignmentOperation(@RequestBody MicroserviceNotificationAssignmentOperationMunicipalityDto data);

	@RequestMapping(method = RequestMethod.POST, value = "/api/notifier/v1/notify/input_request", consumes = APPLICATION_JSON_VALUE)
	public void inputRequest(@RequestBody MicroserviceNotificationInputRequestDto data);

	@RequestMapping(method = RequestMethod.POST, value = "/api/notifier/v1/notify/load_of_inputs", consumes = APPLICATION_JSON_VALUE)
	public void loadOfInputs(@RequestBody MicroserviceNotificationLoadOfInputsDto data);

	@RequestMapping(method = RequestMethod.POST, value = "/api/notifier/v1/notify/input_integrations", consumes = APPLICATION_JSON_VALUE)
	public void inputIntegration(@RequestBody MicroserviceNotificationInputIntegrationsDto data);

	class Configuration {

		@Bean
		Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
			return new SpringFormEncoder(new SpringEncoder(converters));
		}

		@Bean
		@Scope("prototype")
		public Feign.Builder feignBuilder() {
			return Feign.builder();
		}

	}

}
