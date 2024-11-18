package com.rxcthefirst.aws.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;

public class S3Uploader {

    private final S3Client s3Client;

    public S3Uploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void uploadFilesParallel(Path baseDir, List<Path> files, String bucketName, String s3Prefix) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            List<? extends Future<?>> futures = files.stream()
                    .map(file -> executorService.submit(() -> uploadFile(file, baseDir, bucketName, s3Prefix)))
                    .toList();

            // Wait for all uploads to complete
            for (Future<?> future : futures) {
                future.get();
            }

            System.out.println("All files uploaded successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload files to S3", e);
        } finally {
            executorService.shutdown();
        }
    }

    private void uploadFile(Path file, Path baseDir, String bucketName, String s3Prefix) {
        try {
            // Calculate the relative path from the base directory to the file
            Path relativePath = baseDir.relativize(file);

            // Replace OS-specific separators with S3-compatible '/'
            String key = s3Prefix + "/" + relativePath.toString().replace("\\", "/");

            // Upload file to S3
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    file
            );
            System.out.println("Uploaded: " + file + " to S3 as " + key);
        } catch (Exception e) {
            System.err.println("Failed to upload file: " + file + " due to " + e.getMessage());
        }
    }
}