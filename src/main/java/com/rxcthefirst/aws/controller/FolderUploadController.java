package com.rxcthefirst.aws.controller;


import com.rxcthefirst.aws.service.S3Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FolderUploadController {

    private final S3Service s3Service;

    public FolderUploadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }



    @GetMapping("/upload-folder")
    public String uploadFolderToS3(@RequestParam String folderPath, @RequestParam String bucketName, @RequestParam String s3Prefix) {
        try {
            s3Service.uploadFolder(bucketName, folderPath, s3Prefix);
            return "Folder uploaded successfully to bucket: " + bucketName + " with prefix: " + s3Prefix;
        } catch (Exception e) {
            return "Error uploading folder: " + e.getMessage();
        }
    }

    @PostMapping(value = "/upload-zip", consumes = "multipart/form-data")
    public String uploadZipFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bucketName") String bucketName,
            @RequestParam("s3Prefix") String s3Prefix) {
        try {
            // Unzip the file and upload its contents to S3
            s3Service.uploadZipFileToS3(file, bucketName, s3Prefix);
            return "Files uploaded successfully to S3.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to upload files: " + e.getMessage();
        }
    }
}