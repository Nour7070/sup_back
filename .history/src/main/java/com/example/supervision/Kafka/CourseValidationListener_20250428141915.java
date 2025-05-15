package com.example.supervision.Kafka;

import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.services.SupervisionFileService;

import jakarta.transaction.Transactional;

@Component
public class CourseValidationListener {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileService;
    private final CourseValidationProducer validationProducer;
    
    public CourseValidationListener (CourseRepository courseRepository ,
    ChapterRepository chapterRepository ,
    SupervisionFileService fileService ,
    CourseValidationProducer validationProducer) {
        this.courseRepository = courseRepository ;
        this.chapterRepository = chapterRepository;
        this.fileService = fileService;
        this.validationProducer = validationProducer;
    }

    @Transactional
    @KafkaListener(topics = "course-pending-topic", containerFactory = "genericKafkaListenerContainerFactory")
    public void handleCourseValidation(Map<String, Object> courseData) {
        try {
            System.out.print("Données de cours reçues pour validation: {}", courseData);

            UUID courseId = UUID.fromString(courseData.get("courseId").toString());
            String titre = (String) courseData.get("titre");
            String description = (String) courseData.getOrDefault("description", "");
            String langue = (String) courseData.getOrDefault("langue", "");
            String domaine = (String) courseData.getOrDefault("domaine", "");
            Long formateurId = Long.parseLong(courseData.get("formateurId").toString());
            List<Map<String, Object>> chaptersMap = (List<Map<String, Object>>) courseData.getOrDefault("chapters", List.of());

            if (chaptersMap.isEmpty()) {
                throw new IllegalArgumentException("Cours sans chapitre (ID: " + courseId + ")");
            }

            // Supprimer le cours s'il existe déjà
            if (courseRepository.existsById(courseId)) {
                courseRepository.deleteById(courseId);
                courseRepository.flush();
            }

            // Créer le cours
            Cours cours = new Cours();
            cours.setId(courseId);
            cours.setTitre(titre);
            cours.setDescription(description);
            cours.setLangue(langue);
            cours.setDomaine(domaine);
            cours.setFormateurId(formateurId);
            cours.setStatus(CourseStatus.PENDING);

            // Créer les chapitres
            List<Chapter> chapters = processChaptersData(chaptersMap, cours);
            cours.setChapters(chapters);

            // Sauvegarder le cours
            Cours savedCours = courseRepository.save(cours);
            courseRepository.flush();
            System.out.print("Cours {} sauvegardé avec succès en attente de validation"+ savedCours.getId());

            // Télécharger les fichiers pour les chapitres
            downloadFilesForChapters(savedCours.getChapters());
            
        } catch (Exception e) {
            System.out.print("Échec critique de la validation du cours: {}"+ e.getMessage()+ e);
            throw new RuntimeException("Erreur irrécupérable - Le cours n'a pas été persisté", e);
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
                    System.out.print("URL de fichier manquante pour le chapitre {}" + chapter.getId());
                    continue;
                }

                System.out.print("Tentative de téléchargement du fichier: {}", chapter.getFileUrl());
                Resource file = downloadFileFromUrl(chapter.getFileUrl());
                
                // Valider le type de fichier
                String contentType = determineContentType(file);
                if (!isSupportedFileType(contentType)) {
                    log.warn("Format de fichier non supporté pour le chapitre {}: {}", chapter.getId(), contentType);
                    continue;
                }
                
                log.info("Fichier téléchargé avec succès pour le chapitre {}: {}", 
                        chapter.getId(), file.getFilename());

            } catch (Exception e) {
                log.error("Échec du téléchargement pour le chapitre {} (URL: {}). Raison: {}", 
                        chapter.getId(), chapter.getFileUrl(), e.getMessage(), e);
            }
        }
    }

    private Resource downloadFileFromUrl(String fileUrl) {
        int maxRetries = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Tentative #{} de téléchargement depuis: {}", attempt, fileUrl);
                return fileService.loadFileFromUrl(fileUrl);
            } catch (Exception e) {
                lastException = e;
                log.warn("Échec de la tentative #{}: {}", attempt, e.getMessage());
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.error("Toutes les tentatives ont échoué pour {}", fileUrl, lastException);
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
            log.warn("Impossible de déterminer le type de contenu: {}", e.getMessage());
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