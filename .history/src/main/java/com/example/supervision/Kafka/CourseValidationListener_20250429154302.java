/*package com.example.supervision.Kafka;

import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.Langue;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.services.SupervisionFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CourseKafkaListener {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileStorageService;

    public CourseKafkaListener(CourseRepository courseRepository,
                               ChapterRepository chapterRepository,
                               SupervisionFileService fileStorageService) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.fileStorageService = fileStorageService;
    }

 @KafkaListener(
    topics = "course-pending-topic",
    groupId = "course-validation-group",
    containerFactory = "kafkaListenerContainerFactory" // <== ici c'est important !
)
@Transactional
public void listenForCourseValidation(Map<String, Object> courseData) {
    try {
        System.out.println("[CourseKafkaListener] Message reçu : " + courseData);

        String courseIdStr = (String) courseData.get("courseId");
        UUID courseId = UUID.fromString(courseIdStr);
        String titre = (String) courseData.get("titre");
        String description = (String) courseData.get("description");
        String langueStr = (String) courseData.get("langue");
        Langue langue = Langue.valueOf(langueStr);
        String domaine = (String) courseData.get("domaine");
        String formateurIdStr = (String) courseData.get("formateurId");
        Long formateurId = Long.valueOf(formateurIdStr);

        Cours course;
        if (courseRepository.existsById(courseId)) {
            course = courseRepository.findById(courseId).get();
        } else {
            course = new Cours();
            course.setId(courseId);
            course.setTitre(titre);
            course.setDescription(description);
            course.setLangue(langue);
            course.setDomaine(domaine);
            course.setFormateurId(formateurId);
            course = courseRepository.save(course);
        }

        List<Map<String, Object>> chaptersData = (List<Map<String, Object>>) courseData.get("chapters");
        for (Map<String, Object> chapterData : chaptersData) {
            String chapterTitle = (String) chapterData.get("title");
            String chapterDescription = (String) chapterData.get("description");
            String fileUrl = (String) chapterData.get("fileUrl");

            Chapter chapter = new Chapter();
            chapter.setTitle(chapterTitle);
            chapter.setDescription(chapterDescription);
            chapter.setFileUrl(fileUrl);
            chapter.setCours(course);
            chapterRepository.save(chapter);
        }

        courseRepository.save(course);

        System.out.println("[CourseKafkaListener] Cours traité : " + titre);

    } catch (Exception e) {
        System.err.println("[CourseKafkaListener] Erreur : " + e.getMessage());
        e.printStackTrace();
    }
}
}*/
package com.example.supervision.Kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.services.ActivityLogService;
import com.example.supervision.services.FileContentService;
import com.example.supervision.services.SupervisionFileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.springframework.core.io.Resource;



@Service
public class CourseValidationListener {

    private final ObjectMapper objectMapper;
    private final FileContentService fileContentService;
    private final CourseRepository courseRepository;
    private final ActivityLogService activityLogService;
    
    public CourseValidationListener(ObjectMapper objectMapper,
                                   FileContentService fileContentService,
                                   CourseRepository courseRepository,
                                   ActivityLogService activityLogService) {
        this.objectMapper = objectMapper;
        this.fileContentService = fileContentService;
        this.courseRepository = courseRepository;
        this.activityLogService = activityLogService;
    }
    @Transactional
    @KafkaListener(topics = "course-pending-topic", containerFactory = "kafkaListenerContainerFactory")
    public void handleCourseValidation(String message) {
        // Déclarer les variables comme final
        final Long[] formateurId = {null};
        final String[] titre = {""};
        final UUID[] courseId = {null};
    
        try {
            // Désérialisation du message JSON
            Map<String, Object> courseData = objectMapper.readValue(message, new TypeReference<>() {});
            
            // Extraction des données de base
            courseId[0] = UUID.fromString(courseData.get("courseId").toString());
            formateurId[0] = Long.parseLong(courseData.get("formateurId").toString());
            titre[0] = (String) courseData.get("titre");
            
            // Utilisation d'une variable finale pour le lambda
            final UUID finalCourseId = courseId[0];
            
            // Récupération ou création du cours
            Cours cours = courseRepository.findById(finalCourseId).orElseGet(() -> {
                Cours newCours = new Cours();
                newCours.setId(finalCourseId);
                return newCours;
            });
            
            // Mise à jour des propriétés du cours
            cours.setTitre(titre[0]);
            cours.setFormateurId(formateurId[0]);
            cours.setStatus(CourseStatus.PENDING);
            
            // Vérification des chapitres
            List<Map<String, Object>> chaptersMap = (List<Map<String, Object>>) courseData.get("chapters");
            if (chaptersMap == null || chaptersMap.isEmpty()) {
                throw new IllegalArgumentException("Le cours doit contenir au moins un chapitre");
            }
    
            // Nettoyage des anciens chapitres
            cours.getChapters().clear();
            
            // Ajout des nouveaux chapitres
            for (Map<String, Object> chapterMap : chaptersMap) {
                String fileUrl = (String) chapterMap.get("fileUrl");
                String fileName = fileUrl.replace("file://", "");
                
                // Extraction du contenu du fichier
                String content = fileContentService.extractContentFromStoredFile(fileName);
                
                Chapter chapter = new Chapter();
                chapter.setTitle((String) chapterMap.get("titre"));
                chapter.setDescription((String) chapterMap.getOrDefault("description", ""));
                chapter.setFileUrl(fileUrl);
                chapter.setCours(cours);
                cours.getChapters().add(chapter);
            }
            
            // Sauvegarde finale
            Cours savedCours = courseRepository.save(cours);
            
            // Journalisation du succès
            activityLogService.log(
                "COURSE_VALIDATION_SUCCESS",
                "Cours \"" + titre[0] + "\" traité avec succès",
                formateurId[0],
                null,
                savedCours.getId().toString()
            );
            
            System.out.println("Cours validé et sauvegardé : " + savedCours.getId());
            
        } catch (Exception e) {
            System.err.println("Échec de la validation du cours : " + e.getMessage());
            e.printStackTrace();
            
            // Journalisation de l'échec
            activityLogService.log(
                "COURSE_VALIDATION_FAILED",
                "Échec du traitement du cours \"" + titre[0] + "\" : " + e.getMessage(),
                formateurId[0],
                null,
                courseId[0] != null ? courseId[0].toString() : null
            );
            
            // Relance de l'exception pour le réessai Kafka
            throw new RuntimeException("Erreur de validation du cours: " + e.getMessage(), e);
        }
    }