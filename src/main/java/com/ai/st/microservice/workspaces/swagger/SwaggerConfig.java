package com.ai.st.microservice.workspaces.swagger;

import com.google.common.base.Predicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket usersApi() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(usersApiInfo()).select().paths(userPaths())
                .apis(RequestHandlerSelectors.any()).build().useDefaultResponseMessages(false);
    }

    private ApiInfo usersApiInfo() {
        return new ApiInfoBuilder().title("Microservice Workspaces").version("1.0")
                .license("GNU AFFERO GENERAL PUBLIC LICENSE - Version 3").build();
    }

    private Predicate<String> userPaths() {
        return regex("/api.*");
    }

}
