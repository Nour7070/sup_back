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
    String contentType = file.getContentType();
      //Valide le type
    if (!isSupportedContentType(contentType)) {
        throw new RuntimeException("Type de fichier non supporté: " + contentType);
    }
    //Génère un nom unique UUID + nom original
    String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
    String filename = UUID.randomUUID() + "_" + originalFilename;

    //Sauvegarder le fichier dans le dossier upload dir
    Path targetLocation = this.fileStorageLocation.resolve(filename);

    System.out.println("Tentative d'enregistrement du fichier : " + targetLocation);

    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

    if (!Files.exists(targetLocation)) {
        throw new IOException("Fichier non créé : " + targetLocation);
    }

    System.out.println("Fichier enregistré : " + targetLocation);

    //Retourne le nom du fichier stocké
    return filename;
}
    //Valide le type
    private boolean isSupportedContentType(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") || 
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            ||

        contentType.equals("application/octet-stream")
            contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
        );
    }

}