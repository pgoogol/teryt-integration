package com.pgoogol.teryt.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableIntegration
@ConfigurationPropertiesScan
public class TerytIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TerytIntegrationApplication.class, args);
    }

}
