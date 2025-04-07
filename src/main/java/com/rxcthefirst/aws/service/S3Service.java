package com.rxcthefirst.aws.service;

import com.rxcthefirst.aws.dto.S3FileMetadata;
import io.swagger.v3.core.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class S3Service {

    private final S3Client s3Client;

    private final JsonFileReader jsonFileReader;

    // Name of your S3 bucket
    private final String bucketName = "rxcthefirst-testdata";

    @Autowired
    public S3Service(S3Client s3Client, JsonFileReader jsonFileReader) {
        this.s3Client = s3Client;
        this.jsonFileReader = jsonFileReader;
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

    public void uploadFolder(String bucketName, String folderPath, String s3Prefix) {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("The provided path is not a directory: " + folderPath);
        }

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(64); // Adjust thread count as needed

        try {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.isFile()) {
                    executorService.submit(() -> uploadFile(bucketName, file, s3Prefix));
                } else if (file.isDirectory()) {
                    // Recursively upload subfolders
                    uploadFolder(bucketName, file.getAbsolutePath(), s3Prefix + "/" + file.getName());
                }
            }
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.HOURS); // Adjust timeout as needed
            } catch (InterruptedException e) {
                throw new RuntimeException("File upload process was interrupted", e);
            }
        }
    }

    private void uploadFile(String bucketName, File file, String s3Prefix) {
        String s3Key = s3Prefix + "/" + file.getName();
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .build(),
                    file.toPath()
            );
            System.out.println("Uploaded: " + file.getName() + " to S3 key: " + s3Key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + file.getName(), e);
        }
    }

    public void uploadZipFileToS3(MultipartFile zipFile, String bucketName, String s3Prefix) throws Exception {
        // Create temporary directory to extract files
        Path tempDir = Files.createTempDirectory("unzipped");

        try (InputStream is = zipFile.getInputStream();
             ZipInputStream zis = new ZipInputStream(is)) {

            // Extract files from ZIP
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path filePath = tempDir.resolve(entry.getName());
                    Files.createDirectories(filePath.getParent()); // Ensure parent directories exist
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        // Upload extracted files to S3 in parallel
        uploadFolderToS3Parallel(tempDir, bucketName, s3Prefix);

        // Clean up temporary directory
        deleteTempDir(tempDir);
    }

    private void uploadFolderToS3Parallel(Path folderPath, String bucketName, String s3Prefix) {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust thread count as needed
        try {
            Files.walk(folderPath).filter(Files::isRegularFile).forEach(file -> {
                executorService.submit(() -> {
                    String s3Key = s3Prefix + "/" + folderPath.relativize(file).toString();
                    try {
                        s3Client.putObject(
                                PutObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(s3Key)
                                        .build(),
                                file
                        );
                        System.out.println("Uploaded: " + file + " to S3 key: " + s3Key);
                    } catch (Exception e) {
                        System.err.println("Failed to upload file: " + file + " due to " + e.getMessage());
                    }
                });
            });
        } catch (Exception e) {
            throw new RuntimeException("Error uploading files to S3", e);
        } finally {
            executorService.shutdown();
        }
    }
    public void uploadFilesParallel(List<Path> files, String bucketName, String s3Prefix) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            List<? extends Future<?>> futures = files.stream()
                    .map(file -> executorService.submit(() -> uploadFile(file, bucketName, s3Prefix)))
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

    private void uploadFile(Path file, String bucketName, String s3Prefix) {
        String key = s3Prefix + "/" + file.getFileName().toString();
        try {
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

    private void deleteTempDir(Path tempDir) {
        try {
            Files.walk(tempDir)
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            System.err.println("Failed to delete: " + file);
                        }
                    });
        } catch (Exception e) {
            System.err.println("Failed to clean up temporary directory: " + e.getMessage());
        }
    }

    public void uploadFile(String key, String author, List<String> sharedWith, String jsonBody) {
        List<Tag> tags = new ArrayList<>();
        String prefix = "tagged_files/";
        tags.add(Tag.builder().key("author").value(author).build());

        key = prefix + key;
        if (sharedWith != null && !sharedWith.isEmpty()) {
            String sharedStr = String.join(",", sharedWith);
            tags.add(Tag.builder().key("sharedWith").value(sharedStr).build());
        }

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/json")
                .build();

        s3Client.putObject(putRequest, RequestBody.fromString(jsonBody));

        s3Client.putObjectTagging(PutObjectTaggingRequest.builder()
                .bucket(bucketName)
                .key(key)
                .tagging(Tagging.builder().tagSet(tags).build())
                .build());
    }

    public List<S3FileMetadata> listFilesWithTagging(String username) {
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("tagged_files/")
                .build());

        List<S3FileMetadata> result = new ArrayList<>();

        for (S3Object object : listResponse.contents()) {
            String key = object.key();
            String author = null;
            String sharedWith = null;

            try {
                GetObjectTaggingResponse tagResponse = s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());

                for (Tag tag : tagResponse.tagSet()) {
                    if ("author".equalsIgnoreCase(tag.key())) {
                        author = tag.value();
                    } else if ("sharedWith".equalsIgnoreCase(tag.key())) {
                        sharedWith = tag.value();
                    }
                }

                if (username == null ||
                        username.equals(author) ||
                        (sharedWith != null && Arrays.asList(sharedWith.split("_")).contains(username))) {

                    result.add(new S3FileMetadata(
                            key,
                            author,
                            sharedWith,
                            object.size(),
                            object.lastModified().toString(),
                            object.eTag()
                    ));
                }

            } catch (S3Exception e) {
                // Skip object if tagging fails
                System.err.println("Failed to get tags for object " + key + ": " + e.awsErrorDetails().errorMessage());
            }
        }

        return result;
    }
}

