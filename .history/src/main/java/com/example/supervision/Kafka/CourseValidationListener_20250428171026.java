/*package com.example.supervision.Kafka;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.services.SupervisionFileService;
import org.springframework.core.io.Resource;

import jakarta.transaction.Transactional;

@Component
public class CourseValidationListener {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileService;
    
    public CourseValidationListener (CourseRepository courseRepository ,
    ChapterRepository chapterRepository ,
    SupervisionFileService fileService ) {
        this.courseRepository = courseRepository ;
        this.chapterRepository = chapterRepository;
        this.fileService = fileService;
    }

    @Transactional 
    @KafkaListener(
        topics = "course-pending-topic",
        containerFactory = "courseValidationKafkaListenerContainerFactory"
    )
    public void handleCourseValidation(Map<String, Object> courseData) {
        try {
            // Vérification des données
            if (courseData == null || !courseData.containsKey("courseId") || 
                !courseData.containsKey("titre") || !courseData.containsKey("formateurId")) {
                throw new IllegalArgumentException("Données de cours manquantes ou invalides.");
            }
    
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
    
            // Vérification de l'existence du cours
            if (courseRepository.existsById(courseId)) {
                Cours existing = courseRepository.getReferenceById(courseId);
                courseRepository.delete(existing);
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
            System.out.println("Cours sauvegardé avec succès, en attente de validation : " + savedCours.getId());
    
            // Télécharger les fichiers pour les chapitres
            downloadFilesForChapters(savedCours.getChapters());
    
        } catch (Exception e) {
            System.out.println("Échec critique de la validation du cours: " + e.getMessage());
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

                System.out.print("Tentative de téléchargement du fichier: {}" + chapter.getFileUrl());
                Resource file = downloadFileFromUrl(chapter.getFileUrl());
                
                // Valider le type de fichier
                String contentType = determineContentType(file);
                if (!isSupportedFileType(contentType)) {
                    System.out.print("Format de fichier non supporté pour le chapitre {}: {}"+ chapter.getId()+ contentType);
                    continue;
                }
                
                System.out.print("Fichier téléchargé avec succès pour le chapitre {}: {}" +
                        chapter.getId() + file.getFilename());

            } catch (Exception e) {
                System.out.print("Échec du téléchargement pour le chapitre {} (URL: {}). Raison: {}" +
                        chapter.getId() + chapter.getFileUrl() + e.getMessage() + e);
            }
        }
    }

    private Resource downloadFileFromUrl(String fileUrl) {
        int maxRetries = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.print("Tentative #{} de téléchargement depuis: {}" + attempt +fileUrl);
                return fileService.loadFileFromUrl(fileUrl);
            } catch (Exception e) {
                lastException = e;
                System.out.print("Échec de la tentative #{}: {}"+ attempt+ e.getMessage());
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.print("Toutes les tentatives ont échoué pour {}" + fileUrl + lastException);
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
            System.out.print("Impossible de déterminer le type de contenu: {}" + e.getMessage());
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
    */
package com.example.supervision.Kafka;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.Chapter;
import com.example.supervision.repository.CoursRepository;
import com.example.supervision.repository.ChapterRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@EnableKafka
public class CourseValidationListener {

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @KafkaListener(topics = "course-pending-topic", groupId = "supervision-service-group", containerFactory = "courseValidationKafkaListenerContainerFactory")
    public void listenCourseValidation(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            // Récupérer le message au format Map<String, Object>
            Map<String, Object> courseData = record.value();
            System.out.println("Reçu un message pour le cours : " + courseData);

            // Extraire les informations du cours
            UUID courseId = UUID.fromString(courseData.get("courseId").toString());
            String titre = courseData.get("titre").toString();
            String description = courseData.get("description").toString();
            String langue = courseData.get("langue").toString();
            String domaine = courseData.get("domaine").toString();
            Long formateurId = Long.parseLong(courseData.get("formateurId").toString());

            // Créer un objet Cours à partir des données reçues
            Cours cours = new Cours();
            cours.setId(courseId);
            cours.setTitre(titre);
            cours.setDescription(description);
            cours.setLangue(langue);
            cours.setDomaine(domaine);
            cours.setFormateurId(formateurId);
            // Vous pouvez également ajouter d'autres valeurs si nécessaire (comme 'status', 'approvedBy', etc.)

            // Enregistrer le cours dans la base de données
            coursRepository.save(cours);
            System.out.println("Cours enregistré : " + cours);

            // Récupérer et traiter les chapitres
            List<Map<String, Object>> chaptersData = (List<Map<String, Object>>) courseData.get("chapters");
            for (Map<String, Object> chapterData : chaptersData) {
                String chapterTitle = chapterData.get("title").toString();
                String chapterDescription = chapterData.get("description").toString();
                String fileUrl = chapterData.get("fileUrl").toString();

                // Créer un chapitre à partir des données reçues
                Chapter chapter = new Chapter();
                chapter.setCours(cours);
                chapter.setTitre(chapterTitle);
                chapter.setDescription(chapterDescription);
                chapter.setFileUrl(fileUrl);
                // Enregistrer le chapitre dans la base de données
                chapterRepository.save(chapter);
                System.out.println("Chapitre enregistré : " + chapter);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Gérez les erreurs (ex. journalisation, notifications)
        }
    }
}
