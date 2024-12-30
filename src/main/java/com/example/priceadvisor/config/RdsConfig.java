package com.example.priceadvisor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;


@Configuration
public class RdsConfig {
    String regionString = "us-east-1";

    @Bean
    public RdsClient rdsClient() {
        return RdsClient.builder()
                .region(Region.of(regionString))
                .build();
    }
}