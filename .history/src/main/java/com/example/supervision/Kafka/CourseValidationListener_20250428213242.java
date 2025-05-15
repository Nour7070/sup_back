package com.example.supervision.Kafka;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Chapter;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.ChapterRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hibernate.PropertyValueException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.supervision.services.SupervisionFileService;

import jakarta.transaction.Transactional;

/*@Service
@EnableKafka
public class CourseValidationListener {

    @Autowired
    private CourseRepository coursRepository;
    private ChapterRepository chapterRepository;


    @Autowired
    private SupervisionFileService fileService;

@Transactional
@KafkaListener(topics = "course-pending-topic", groupId = "supervision-service-group", 
               containerFactory = "courseValidationKafkaListenerContainerFactory")
public void consumeCourseData(Map<String, Object> message) {
    int maxRetries = 3;
    int attempt = 0;
    
    while (attempt < maxRetries) {
        try {
            String courseIdString = (String) message.get("courseId");
            UUID courseId = UUID.fromString(courseIdString);
            
            Optional<Cours> existingCoursOptional = coursRepository.findById(courseId);
            Cours cours;
            boolean isNewCours = !existingCoursOptional.isPresent();
            
            if (isNewCours) {
                cours = new Cours();
                cours.setId(courseId);
                cours.setVersion(0L); // Initialisation explicite pour les nouveaux cours
            } else {
                cours = existingCoursOptional.get();
                
                // Supprimer les chapitres existants explicitement
                chapterRepository.deleteAll(cours.getChapters());
                cours.getChapters().clear();
            }
            
            // Mise à jour des propriétés
            cours.setTitre((String) message.get("titre"));
            cours.setDescription((String) message.get("description"));
            cours.setLangue((String) message.get("langue"));
            cours.setDomaine((String) message.get("domaine"));
            cours.setStatus(CourseStatus.PENDING);
            
            // Traitement des chapitres
            List<Map<String, Object>> chaptersData = (List<Map<String, Object>>) message.get("chapters");
            List<Chapter> chapters = processChaptersData(chaptersData, cours);
            
            if (cours.getChapters() == null) {
                cours.setChapters(new ArrayList<>());
            }
            cours.getChapters().addAll(chapters);
            
            // Télécharger les fichiers
            downloadFilesForChapters(chapters);

            // Sauvegarder
            Cours saved = coursRepository.save(cours);
            
            System.out.println("Cours sauvegardé avec version: " + saved.getVersion());
            
            return;
            
        } catch (ObjectOptimisticLockingFailureException e) {
            attempt++;
            if (attempt == maxRetries) {
                System.err.println("Échec après " + maxRetries + " tentatives");
                throw e;
            }
            try {
                Thread.sleep(100 * attempt);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interruption", ie);
            }
        } catch (PropertyValueException e) {
            // Gestion spécifique de l'erreur de version
            System.err.println("Erreur de version: " + e.getMessage());
            throw new RuntimeException("Problème avec la version de l'entité", e);
        }
    }
}
private List<Chapter> processChaptersData(List<Map<String, Object>> chaptersMap, Cours cours) {
    return chaptersMap.stream()
        .map(chapterMap -> {
            Chapter chapter = new Chapter();
            chapter.setTitle((String) chapterMap.get("title"));
            chapter.setDescription((String) chapterMap.getOrDefault("description", ""));
            chapter.setFileUrl((String) chapterMap.get("fileUrl"));
            chapter.setCours(cours);
            return chapter;
        })
        .collect(Collectors.toList());
}

private void downloadFilesForChapters(List<Chapter> chapters) {
    for (Chapter chapter : chapters) {
        try {
            if (chapter.getFileUrl() == null || chapter.getFileUrl().isEmpty()) {
                System.out.println("URL de fichier manquante pour le chapitre " + chapter.getId());
                continue;
            }

            System.out.println("Tentative de téléchargement du fichier: " + chapter.getFileUrl());
            Resource file = downloadFileFromUrl(chapter.getFileUrl());
            
            // Valider le type de fichier
            String contentType = determineContentType(file);
            if (!isSupportedFileType(contentType)) {
                System.out.println("Format de fichier non supporté pour le chapitre " + chapter.getId() + ": " + contentType);
                continue;
            }
            
            System.out.println("Fichier téléchargé avec succès pour le chapitre " + chapter.getId() + ": " + file.getFilename());

        } catch (Exception e) {
            System.out.println("Échec du téléchargement pour le chapitre " + chapter.getId() + " (URL: " + chapter.getFileUrl() + "). Raison: " + e.getMessage());
        }
    }
}
*/
/*private Resource downloadFileFromUrl(String fileUrl) {
    int maxRetries = 3;
    Exception lastException = null;

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            System.out.println("Tentative #" + attempt + " de téléchargement depuis: " + fileUrl);
            return fileService.loadFileFromUrl(fileUrl);
        } catch (Exception e) {
            lastException = e;
            System.out.println("Échec de la tentative #" + attempt + ": " + e.getMessage());
            try {
                Thread.sleep(1000 * attempt);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    System.out.println("Toutes les tentatives ont échoué pour " + fileUrl + " : " + lastException);
    throw new RuntimeException("Échec du téléchargement après " + maxRetries + " tentatives", lastException);
}*/
/* 
private Resource downloadFileFromUrl(String fileUrl) throws Exception {
    int maxRetries = 3;
    Exception lastException = null;

    // Traiter les différents types d'URLs
    if (fileUrl.startsWith("file://")) {
        // C'est un fichier local, extraire le chemin
        String filePath = fileUrl.substring(7); // Supprimer "file://"
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("Tentative #" + attempt + " de chargement du fichier local: " + filePath);
                return fileService.loadFileAsResource(filePath);
            } catch (Exception e) {
                lastException = e;
                System.out.println("Échec de la tentative #" + attempt + ": " + e.getMessage());
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    } else {
        // C'est une URL web ou un nom de fichier simple
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("Tentative #" + attempt + " de téléchargement depuis: " + fileUrl);
                return fileService.loadFileFromUrl(fileUrl);
            } catch (Exception e) {
                lastException = e;
                System.out.println("Échec de la tentative #" + attempt + ": " + e.getMessage());
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    System.out.println("Toutes les tentatives ont échoué pour " + fileUrl + " : " + lastException);
    throw new RuntimeException("Échec du téléchargement après " + maxRetries + " tentatives", lastException);
}

private String determineContentType(Resource file) {
    try {
        String filename = file.getFilename();
        if (filename != null) {
            if (filename.endsWith(".pdf")) {
                return "application/pdf";
            } else if (filename.endsWith(".pptx")) {
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            }
        }
        return "application/octet-stream";
    } catch (Exception e) {
        System.out.println("Impossible de déterminer le type de contenu: " + e.getMessage());
        return "application/octet-stream";
    }
}

private boolean isSupportedFileType(String contentType) {
    return contentType != null && (
        contentType.equals("application/pdf") ||
        contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
        contentType.equals("application/octet-stream")
    );
}
}
