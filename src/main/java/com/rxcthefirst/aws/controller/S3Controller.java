package com.rxcthefirst.aws.controller;

import com.rxcthefirst.aws.dto.S3FileMetadata;
import com.rxcthefirst.aws.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.Tag;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    @Autowired
    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    // Upload a file to S3
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return s3Service.uploadFile(file);
    }

    // Download a file from S3
    @GetMapping("/download")
    public byte[] downloadFile(@RequestParam("fileName") String fileName) {
        return s3Service.downloadFile(fileName);
    }

    // Endpoint to list all files in the S3 bucket
    @GetMapping("/list")
    public List<String> listFiles(@RequestParam(value = "prefix", required = false, defaultValue = "") String prefix) {
        return s3Service.listFiles(prefix);
    }

    @GetMapping("/listMetadata")
    public List<S3Service.S3ObjectMetadata> listMetadata(@RequestParam(value = "prefix", required = false, defaultValue = "") String prefix) {
        return s3Service.listFilesWithBasicMetadata(prefix);
    }

    @PostMapping("/uploadFileWithTagging")
    public ResponseEntity<String> uploadFileWithTagging(
            @RequestBody String jsonBody,
            @RequestParam String key,
            @RequestParam String author,
            @RequestParam(required = false) List<String> sharedWith
    ) {
        try {
            s3Service.uploadFile(key, author, sharedWith, jsonBody);
            return ResponseEntity.ok("File uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/listFilesWithTagging")
    public ResponseEntity<List<S3FileMetadata>> listFilesWithTagging(@RequestParam(required = false) String username) {
        System.out.println("Username: " + username);
        return ResponseEntity.ok(s3Service.listFilesWithTagging(username));
    }
}