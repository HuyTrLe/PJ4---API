// package com.mytech.api.config;

// import java.util.Arrays;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import springfox.documentation.builders.RequestHandlerSelectors;
// import springfox.documentation.spi.DocumentationType;
// import springfox.documentation.spring.web.plugins.Docket;
// import springfox.documentation.swagger2.annotations.EnableSwagger2;
// import springfox.documentation.service.ApiKey;
// import springfox.documentation.builders.PathSelectors;

// @Configuration
// @EnableSwagger2
// public class SwaggerConfig {

// @Bean
// public Docket api() {
// return new Docket(DocumentationType.SWAGGER_2)
// .select()
// .apis(RequestHandlerSelectors.any())
// .paths(PathSelectors.any())
// .build()
// .securitySchemes(Arrays.asList(apiKey()));
// }

// private ApiKey apiKey() {
// return new ApiKey("Bearer Token", "Authorization", "header");
// }
// }
