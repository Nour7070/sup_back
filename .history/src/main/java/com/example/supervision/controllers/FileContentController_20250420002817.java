package com.example.supervision.controllers;

import com.example.supervision.classes.FileContent;
import com.example.supervision.services.FileContentService;

import java.io.IOException;
import java.nio.file.Paths;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/file-content")
public class FileContentController {

    private final FileContentService fileContentService; 
    private final String UPLOAD_DIR = "/home/nour/Téléchargements/supervision/back/uploads/supervision/";

    public FileContentController(FileContentService fileContentService) {
        this.fileContentService = fileContentService;
    }

    @PostMapping("/extract")
    public ResponseEntity<FileContent> extractFileContent(@RequestParam("file") MultipartFile file) {
        try {
            String content = fileContentService.extractContentFromFile(file);
            return ResponseEntity.ok(new FileContent(
                file.getOriginalFilename(),
                content,
                file.getContentType()
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/chapter")
    public ResponseEntity<FileContent> getChapterContent(
            @RequestParam String fileUrl) {
        try {
            String content = fileContentService.extractContentFromStoredFile(fileUrl);
            return ResponseEntity.ok(new FileContent(
                fileUrl,
                content,
                fileUrl.endsWith(".pdf") ? "application/pdf" : "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            ));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // Chemin absolu et normalisé
            Path filePath = Paths.get(UPLOAD_DIR, filename).toAbsolutePath().normalize();
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                System.err.println("Fichier non trouvé: " + filePath);
                return ResponseEntity.notFound().build();
            }

            System.out.println("Fichier trouvé: " + filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'accès au fichier: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}