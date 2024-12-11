package com.example.priceadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SNSConfig {

    String regionString = "us-east-1";

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.of(regionString))
                .build();
    }
}
