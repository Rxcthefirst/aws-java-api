package com.rxcthefirst.aws.service;

import org.springframework.stereotype.Service;

@Service
public class MyBusinessService {

    private final JsonFileReader jsonFileReader;

    // Constructor Injection (Recommended)
    public MyBusinessService(JsonFileReader jsonFileReader) {
        this.jsonFileReader = jsonFileReader;
    }

    public void processJsonFile(String fileName) {
        try {
            // Use JsonFileReader to read the file
            String jsonContent = jsonFileReader.readJsonFile(fileName);

            // Perform processing on the JSON content
            System.out.println("Processing file: " + fileName);
            System.out.println(jsonContent);

            // Example: Parse JSON or perform business logic
            // You can use libraries like Jackson or Gson to parse JSON here
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }
}