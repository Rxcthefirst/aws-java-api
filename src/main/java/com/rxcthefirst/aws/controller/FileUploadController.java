package com.rxcthefirst.aws.controller;

import com.rxcthefirst.aws.service.S3Uploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private ZipFileExtractor zipFileExtractor;

    @Autowired
    private S3Uploader s3Uploader;

    @PostMapping(value = "/upload-zip", consumes = "multipart/form-data")
    public String uploadZipFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bucketName") String bucketName,
            @RequestParam("s3Prefix") String s3Prefix) {
        try {
            // Extract files into a temporary directory
            Path tempDir;
            List<Path> extractedFiles;
            try (InputStream zipInputStream = file.getInputStream()) {
                extractedFiles = zipFileExtractor.extractZipToTempDir(zipInputStream);
                tempDir = extractedFiles.get(0).getParent().getParent(); // Get the root of the extracted directory
            }

            // Upload files to S3
            s3Uploader.uploadFilesParallel(tempDir, extractedFiles, bucketName, s3Prefix);

            return "Files uploaded successfully!";
        } catch (Exception e) {
            throw new RuntimeException("Failed to process the ZIP file", e);
        }
    }
}