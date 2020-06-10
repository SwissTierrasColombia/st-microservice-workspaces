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

import com.ai.st.microservice.workspaces.dto.reports.MicroserviceReportInformationDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceRequestReportDownloadSupplyIndividualDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceRequestReportDownloadSupplyTotalDto;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "st-microservice-reports", configuration = ReportFeignClient.Configuration.class)
public interface ReportFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "/api/reports/v1/reports/download-supplies-individual", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceReportInformationDto createReportDownloadSuppliesIndiviual(
			@RequestBody MicroserviceRequestReportDownloadSupplyIndividualDto data);

	@RequestMapping(method = RequestMethod.POST, value = "/api/reports/v1/reports/download-supplies-total", consumes = APPLICATION_JSON_VALUE)
	public MicroserviceReportInformationDto createReportDownloadSuppliesTotal(
			@RequestBody MicroserviceRequestReportDownloadSupplyTotalDto data);

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
