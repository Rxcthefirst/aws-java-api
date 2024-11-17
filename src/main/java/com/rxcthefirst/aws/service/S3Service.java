package com.rxcthefirst.aws.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Client s3Client;

    // Name of your S3 bucket
    private final String bucketName = "rxcthefirst-testdata";

    @Autowired
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // Upload a file to S3
    public String uploadFile(MultipartFile file) {
        String keyName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(keyName)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));
            return "File uploaded successfully. Key: " + keyName;
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3", e);
        }
    }

    // Download a file from S3
    public byte[] downloadFile(String fileName) {
        try {
            ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());

            return object.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error downloading file from S3", e);
        }
    }

    public List<String> listFiles(String prefix) {
        ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .delimiter("/")
                .build();

        ListObjectsV2Response listObjResponse = s3Client.listObjectsV2(listObjectsReqManual);

//        return listObjResponse.contents()
//                .stream()
//                .map(S3Object::key)  // Retrieve the key (file name) for each object
//                .collect(Collectors.toList());

        // Get the common prefixes (folders) at the current level
        return listObjResponse.commonPrefixes()
                .stream()
                .map(commonPrefix -> commonPrefix.prefix())  // Extract the folder name (prefix)
                .collect(Collectors.toList());
    }

    // Object metadata structure to return from the API
    public static class S3ObjectMetadata {
        private String key;
        private long size;
        private String lastModified;
        private String eTag;

        // Constructor to initialize metadata from the S3 response
        public S3ObjectMetadata(String key, long size, String lastModified, String eTag) {
            this.key = key;
            this.size = size;
            this.lastModified = lastModified;
            this.eTag = eTag;
        }

        // Getters for serialization
        public String getKey() { return key; }
        public long getSize() { return size; }
        public String getLastModified() { return lastModified; }
        public String getETag() { return eTag; }
    }

    // Method to list objects and their basic metadata in a single API call
    public List<S3ObjectMetadata> listFilesWithBasicMetadata(String prefix) {
        ListObjectsV2Request listObjectsReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)  // Specify the prefix if needed (e.g., "folder/")
                .delimiter("/")  // Simulate folder structure
                .build();

        ListObjectsV2Response listObjResponse = s3Client.listObjectsV2(listObjectsReq);

        // Fetch basic metadata for each object
        return listObjResponse.contents()
                .stream()
                .map(s3Object -> new S3ObjectMetadata(
                        s3Object.key(),
                        s3Object.size(),
                        s3Object.lastModified().toString(),
                        s3Object.eTag()
                ))
                .collect(Collectors.toList());
    }


}
