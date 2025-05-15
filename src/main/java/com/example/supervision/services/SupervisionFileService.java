package com.example.supervision.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupervisionFileService {
    private final Path fileStorageLocation;

    public SupervisionFileService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    
        try {
            Files.createDirectories(this.fileStorageLocation);
            if (!Files.isWritable(this.fileStorageLocation)) {
                throw new IOException("Le répertoire n'est pas accessible en écriture : " + this.fileStorageLocation);
            }
    
            System.out.println("Répertoire d'upload prêt : " + this.fileStorageLocation);
    
        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors de la création ou de l'accès au répertoire : " + uploadDir, ex);
        }
    }

public String storeFile(MultipartFile file) throws IOException {
    String contentType = file.getContentType();
    if (!isSupportedContentType(contentType)) {
        throw new RuntimeException("Type de fichier non supporté: " + contentType);
    }
    String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
    String filename = UUID.randomUUID() + "_" + originalFilename;

    Path targetLocation = this.fileStorageLocation.resolve(filename);

    System.out.println("Tentative d'enregistrement du fichier : " + targetLocation);

    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

    if (!Files.exists(targetLocation)) {
        throw new IOException("Fichier non créé : " + targetLocation);
    }

    System.out.println("Fichier enregistré : " + targetLocation);

    return filename;
}
    private boolean isSupportedContentType(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") || 
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")||
            contentType.equals("application/octet-stream")
        );
    }

}