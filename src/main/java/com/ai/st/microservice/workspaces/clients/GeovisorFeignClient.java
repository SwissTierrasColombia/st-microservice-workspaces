package com.ai.st.microservice.workspaces.clients;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ai.st.microservice.workspaces.dto.geovisor.MicroserviceDataMapDto;
import com.ai.st.microservice.workspaces.dto.geovisor.MicroserviceSetupMapDto;

import feign.Feign;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(value = "geovisor", configuration = GeovisorFeignClient.Configuration.class, url = "${geoapi.url}")
public interface GeovisorFeignClient {

    @PostMapping(value = "/st_geocreatefastcontext", consumes = APPLICATION_JSON_VALUE)
    MicroserviceDataMapDto setupMap(@RequestBody MicroserviceSetupMapDto data);

    class Configuration {

        @Bean
        Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
            return new SpringFormEncoder(new SpringEncoder(converters));
        }

        @Bean
        public ErrorDecoder errorDecoder() {
            return new CustomErrorDecoder();
        }

        @Bean
        @Scope("prototype")
        public Feign.Builder feignBuilder() {
            return Feign.builder();
        }

    }

}