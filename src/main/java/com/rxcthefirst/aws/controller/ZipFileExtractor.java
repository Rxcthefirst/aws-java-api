package com.rxcthefirst.aws.controller;

import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileExtractor {

    public List<Path> extractZipToTempDir(InputStream zipInputStream) throws Exception {
        Path tempDir = Files.createTempDirectory("unzipped");
        List<Path> extractedFiles = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path filePath = tempDir.resolve(entry.getName());
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                    extractedFiles.add(filePath);
                    System.out.println("Extracted: " + filePath);
                }
            }
        }

        if (extractedFiles.isEmpty()) {
            throw new RuntimeException("No files found in ZIP!");
        }

        return extractedFiles;
    }
}