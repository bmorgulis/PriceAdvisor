package com.example.priceadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SnsConfig {

    // Create an SNS client
    @Bean
    public SnsClient snsClient() {
        String regionString = "us-east-1";

        return SnsClient.builder()
                .region(Region.of(regionString))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

