package com.example.supervision.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

@Configuration
public class FileStorageConfig {
    @Value("${file.upload-dir}") 
    private String uploadDir;
    
    public void init() throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
    }
}