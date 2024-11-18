package com.rxcthefirst.aws.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FolderContentsController {

    @GetMapping("/list-folder-contents")
    public List<String> listFolderContents(@RequestParam String folderPath) {
        List<String> fileList = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath))) {
            for (Path path : stream) {
                fileList.add(path.toString());
            }
        } catch (IOException | DirectoryIteratorException e) {
            throw new RuntimeException("Failed to read the folder contents: " + e.getMessage());
        }

        return fileList;
    }
}
