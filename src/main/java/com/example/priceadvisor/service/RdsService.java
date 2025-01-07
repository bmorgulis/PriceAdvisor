package com.example.priceadvisor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;

//most of this file is from chatgpt which told me how to start an RDS instance and get its status
@Service
public class RdsService {
    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private String region = "us-east-1";

    private final RdsClient rdsClient;
    private final String dbInstanceIdentifier;

    public RdsService(@Value("${spring.datasource.url}") String datasourceUrl, RdsClient rdsClient) {
        this.rdsClient = rdsClient;
        this.dbInstanceIdentifier = getRdsInstanceIdentifier(datasourceUrl);
    }

    public String getRdsStatus() {
        DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder()
                .dbInstanceIdentifier(dbInstanceIdentifier)
                .build();

        DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);
        return response.dbInstances()
                .stream()
                .findFirst()
                .map(DBInstance::dbInstanceStatus)
                .orElse("DB instance not found");
    }

    public void startRdsInstance() {
        StartDbInstanceRequest request = StartDbInstanceRequest.builder()
                .dbInstanceIdentifier(dbInstanceIdentifier)
                .build();

        rdsClient.startDBInstance(request);
    }

    private String getRdsInstanceIdentifier(String datasourceUrl) {
        if (datasourceUrl != null && datasourceUrl.contains("//")) {
            String part = datasourceUrl.split("//")[1].split("\\.")[0]; // Extract the first part of the URL as the instance identifier gets part after // and before .
            return part;
        } else {
            throw new IllegalStateException("Datasource URL is not properly configured.");
        }
    }

}
