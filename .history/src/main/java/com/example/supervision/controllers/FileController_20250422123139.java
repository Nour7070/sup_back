package com.example.supervision.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.supervision.services.FileContentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping("/files")
public class FileController {
    
    private final String fileBasePath;
    private final String uploadDir;

   public FileContentController(
        FileContentService fileContentService,
        @Value("${file.upload-dir}") String uploadDir
    ) {
        this.fileContentService = fileContentService;
        this.uploadDir = uploadDir;
    }

    // Remplacer les utilisations de UPLOAD_DIR par uploadDir
    // ...
}
Vérification supplémentaire
Vous devriez également vérifier la classe FileService (méthode storeFile) pour vous assurer qu'elle utilise bien la propriété file.upload-dir pour enregistrer les fichiers.
Est-ce que vous pouvez partager le code de la méthode storeFile du FileService pour que je puisse vérifier si le problème vient de là?RéessayerClaude n'a pas encore la capacité d'exécuter le code qu'il génère.Claude peut faire des erreurs. Assurez-vous de vérifier ses réponses.

    @GetMapping("/{folder}/{fileName:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String folder,
            @PathVariable String fileName) throws IOException {

        // Utilisation du chemin injecté
        Path filePath = Paths.get(fileBasePath, folder).resolve(fileName).normalize();
        File file = filePath.toFile();

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.length())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
}