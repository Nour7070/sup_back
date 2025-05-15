package com.example.supervision.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils; 

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/*@Service
public class SupervisionFileService {
    private final Path fileStorageLocation;
    private final Path rootLocation = Paths.get("uploads");

    public SupervisionFileService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Erreur création répertoire supervision", ex);
        }
    }

    public Resource loadFileFromUrl(String fileUrl) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Resource> response = restTemplate.getForEntity(fileUrl, Resource.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Échec du téléchargement: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur de chargement", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));
        return "/uploads/" + filename; 
    }
}*/
@Service
public class SupervisionFileService {
    private final Path fileStorageLocation;

    public SupervisionFileService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Erreur création répertoire: " + uploadDir, ex);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        // Validation du type de fichier
        String contentType = file.getContentType();
        if (!isSupportedContentType(contentType)) {
            throw new RuntimeException("Type de fichier non supporté: " + contentType);
        }

        // Génération du nom de fichier sécurisé
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "_" + originalFilename;
        
        // Stockage dans le répertoire configuré
        Path targetLocation = this.fileStorageLocation.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        return filename; // Retourne uniquement le nom du fichier
    }

    private boolean isSupportedContentType(String contentType) {
        /*return contentType != null && (
            contentType.equals("application/pdf") || 
            contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
        );*/
        return contentType != null && (
            contentType.equals("application/pdf") || 
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
        );
    }

    // Méthode optionnelle pour charger un fichier
    public Resource loadFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non trouvé ou illisible: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur de chargement: " + filename, e);
        }
    }
}