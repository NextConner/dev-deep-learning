package com.jtcool.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("JtCool ERP & WMS API")
                .version("1.0.1")
                .description("企业 ERP 与仓储管理系统 API 文档")
                .contact(new Contact().name("JtCool Team")));
    }

    @Bean
    public GroupedOpenApi omsApi() {
        return GroupedOpenApi.builder()
            .group("OMS-订单管理")
            .pathsToMatch("/oms/**")
            .build();
    }

    @Bean
    public GroupedOpenApi wmsApi() {
        return GroupedOpenApi.builder()
            .group("WMS-仓库管理")
            .pathsToMatch("/wms/**")
            .build();
    }

    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
            .group("Product-产品档案")
            .pathsToMatch("/product/**")
            .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
            .group("System-系统管理")
            .pathsToMatch("/system/**")
            .build();
    }
}
