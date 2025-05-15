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
import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.services.ActivityLogService;
import com.example.supervision.services.FileContentService;
import com.example.supervision.services.SupervisionFileService;

import jakarta.transaction.Transactional;

import org.springframework.core.io.Resource;

@Component
public class CourseValidationListener {

    private final CourseRepository courseRepository;
    private final SupervisionFileService supervisionFileService;
    private final FileContentService fileContentService;
    private final ActivityLogService activityLogService;

    public CourseValidationListener(CourseRepository courseRepository,
                                 SupervisionFileService supervisionFileService,
                                 FileContentService fileContentService,
                                 ActivityLogService activityLogService) {
        this.courseRepository = courseRepository;
        this.supervisionFileService = supervisionFileService;
        this.fileContentService = fileContentService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    @KafkaListener(topics = "course-pending-topic", containerFactory = "genericKafkaListenerContainerFactory")
    public void handleCourseValidation(Map<String, Object> courseData) {
        try {
            System.out.println("Données reçues : " + courseData);

            // Extraction des données de base
            UUID courseId = UUID.fromString(courseData.get("courseId").toString());
            String titre = (String) courseData.get("titre");
            Long formateurId = Long.parseLong(courseData.get("formateurId").toString());
            List<Map<String, Object>> chaptersMap = (List<Map<String, Object>>) courseData.getOrDefault("chapters", List.of());

            if (chaptersMap.isEmpty()) {
                throw new IllegalArgumentException("Cours sans chapitre (ID: " + courseId + ")");
            }

            // Suppression si cours existant
            if (courseRepository.existsById(courseId)) {
                courseRepository.deleteById(courseId);
                courseRepository.flush();
            }

            // Création du cours
            Cours cours = new Cours();
            cours.setId(courseId);
            cours.setTitre(titre);
            cours.setFormateurId(formateurId);
            cours.setStatus(CourseStatus.PENDING);

            // Traitement des chapitres
            List<Chapter> chapters = new ArrayList<>();
            for (Map<String, Object> chapterMap : chaptersMap) {
                String fileUrl = (String) chapterMap.get("fileUrl");
                String fileName = fileUrl.replace("file://", "");
                
                // Validation et extraction du contenu
                String content = fileContentService.extractContentFromStoredFile(fileName);
                
                Chapter chapter = new Chapter();
                chapter.setTitle((String) chapterMap.get("title"));
                chapter.setDescription((String) chapterMap.get("description", ""));
                chapter.setFileUrl(fileUrl);
                chapter.setContent(content); // Stockage du contenu extrait
                chapter.setCours(cours);
                
                chapters.add(chapter);
            }

            cours.setChapters(chapters);
            Cours savedCours = courseRepository.save(cours);
            
            // Log d'activité
            activityLogService.log(
                "COURSE_VALIDATION_PROCESSED",
                "Cours \"" + titre + "\" traité pour validation",
                formateurId,
                courseId,
                null
            );

            System.out.println("Cours validé et sauvegardé : " + savedCours.getId());

        } catch (Exception e) {
            System.err.println("Échec de la validation du cours : " + e.getMessage());
            activityLogService.log(
                "COURSE_VALIDATION_FAILED",
                "Échec du traitement du cours : " + e.getMessage(),
                null,
                courseData.get("courseId") != null ? UUID.fromString(courseData.get("courseId").toString()) : null,
                null
            );
            throw new RuntimeException("Erreur de validation du cours", e);
        }
    }
}