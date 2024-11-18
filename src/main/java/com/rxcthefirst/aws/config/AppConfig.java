package com.rxcthefirst.aws.config;

import com.rxcthefirst.aws.controller.ZipFileExtractor;
import com.rxcthefirst.aws.service.S3Uploader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AppConfig {


    @Bean
    public ZipFileExtractor zipFileExtractor() {
        return new ZipFileExtractor();
    }

    @Bean
    public S3Uploader s3Uploader(S3Client s3Client) {
        return new S3Uploader(s3Client);
    }
}