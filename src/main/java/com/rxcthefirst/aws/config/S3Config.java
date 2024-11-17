package com.rxcthefirst.aws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        return S3Client.builder()
                .region(Region.US_EAST_1)  // Specify your region here
                .credentialsProvider(credentialsProvider)
                .build();
    }
}