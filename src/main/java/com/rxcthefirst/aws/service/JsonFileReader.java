package com.rxcthefirst.aws.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class JsonFileReader {

    public String readJsonFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("json-schemas/" + fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + fileName, e);
        }
    }
}
