package com.example.supervision.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;

import com.example.supervision.services.SupervisionFileService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files")
public class FileController {
    
    private final SupervisionFileService fileService ;
    private final String uploadDir;

    /

    public FileController(@Value("${file.upload-dir}") String uploadDir ,
    SupervisionFileService fileService) {
        this.uploadDir = uploadDir;
        this.fileService = fileService;
    }


    @GetMapping("/api/file-content/{fileName}")
public ResponseEntity<Resource> getFileContent(@PathVariable String fileName) {
    try {
        // Utiliser le chemin configuré dans application.properties
        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists()) {
            // Déterminer le content type
            String contentType = determineContentType(fileName);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        } else {
            System.out.println("Fichier non trouvé: " + filePath.toString());
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
    @GetMapping(value = "/api/file-download/**")
public ResponseEntity<Resource> downloadFileWithComplexName(HttpServletRequest request) {
    try {
        String requestURL = request.getRequestURL().toString();
        String filename = requestURL.substring(requestURL.indexOf("/api/file-download/") + "/api/file-download/".length());
        
        System.out.println("Tentative de téléchargement du fichier: " + filename);
        
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists()) {
            String contentType = determineContentType(filename);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
            
            return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
        } else {
            System.out.println("Fichier non trouvé: " + filePath.toString());
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

    private String determineContentType(String filename) {
        if (filename.toLowerCase().endsWith(".pdf")) {
            return "application/pdf";
        } else if (filename.toLowerCase().endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
}