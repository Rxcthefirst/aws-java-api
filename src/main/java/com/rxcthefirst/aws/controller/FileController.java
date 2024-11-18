package com.rxcthefirst.aws.controller;

import com.rxcthefirst.aws.service.MyBusinessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileController {

    private final MyBusinessService myBusinessService;

    public FileController(MyBusinessService myBusinessService) {
        this.myBusinessService = myBusinessService;
    }

    @GetMapping("/process-file")
    public String processFile(@RequestParam String fileName) {
        myBusinessService.processJsonFile(fileName);
        return "File processed: " + fileName;
    }
}