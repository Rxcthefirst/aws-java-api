package com.rxcthefirst.aws.controller;

import com.rxcthefirst.aws.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}