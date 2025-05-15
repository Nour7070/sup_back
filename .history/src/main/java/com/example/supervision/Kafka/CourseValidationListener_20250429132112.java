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
import com.example.supervision.services.SupervisionFileService;

import jakarta.transaction.Transactional;

import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
public class CourseValidationListener {

    private final CourseRepository courseRepository;
    private final SupervisionFileService supervisionFileService;

    public

    @Transactional
    @KafkaListener(topics = "course-pending-topic", containerFactory = "genericKafkaListenerContainerFactory")
    public void handleCourseValidation(Map<String, Object> courseData) {
        try {
            log.info("Données reçues : {}", courseData);

            UUID courseId = UUID.fromString(courseData.get("courseId").toString());
            String titre = (String) courseData.get("titre");
            Long formateurId = Long.parseLong(courseData.get("formateurId").toString());
            List<Map<String, Object>> chaptersMap = (List<Map<String, Object>>) courseData.getOrDefault("chapters",
                    List.of());

            if (chaptersMap.isEmpty()) {
                throw new IllegalArgumentException("Cours sans chapitre (ID: " + courseId + ")");
            }

            if (courseRepository.existsById(courseId)) {
                courseRepository.deleteById(courseId);
                courseRepository.flush();
            }

            Cours cours = new Cours();
            cours.setId(courseId);
            cours.setTitre(titre);
            cours.setFormateurId(formateurId);
            cours.setStatus(CourseStatus.PENDING);

            List<Chapter> chapters = chaptersMap.stream()
                    .map(chapterMap -> {
                        Chapter chapter = new Chapter();
                        chapter.setTitle((String) chapterMap.get("title"));
                        chapter.setDescription((String) chapterMap.get("description"));
                        chapter.setFileUrl((String) chapterMap.get("fileUrl"));
                        chapter.setCours(cours);
                        return chapter;
                    })
                    .collect(Collectors.toList());

            cours.setChapters(chapters);

            try {

                Cours savedCours = courseRepository.save(cours);
                courseRepository.flush();
                log.info("Sauvegarde confirmée en base pour le cours {}", savedCours.getId());

                downloadFilesForChapters(savedCours.getChapters());
            } catch (Exception e) {
                log.error("Échec de la persistance : ", e);
                throw e;
            }
        } catch (Exception e) {
            log.error("Échec critique de la validation du cours : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur irrécupérable - Le cours n'a pas été persisté", e);
        }
    }

   
    private void downloadFilesForChapters(List<Chapter> chapters) {
        for (Chapter chapter : chapters) {
            try {
                if (chapter.getFileUrl() == null || chapter.getFileUrl().isEmpty()) {
                    log.warn("URL de fichier manquante pour le chapitre {}", chapter.getId());
                    continue;
                }

                log.info("Tentative de téléchargement du fichier: {}", chapter.getFileUrl());
                Resource file = downloadFileFromUrl(chapter.getFileUrl());
                log.info("Fichier téléchargé avec succès pour le chapitre {} : {}",
                        chapter.getId(), file.getFilename());

            } catch (Exception e) {
                log.error("Échec du téléchargement pour le chapitre {} (URL: {}). Raison : {}",
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
                return supervisionFileService.loadFileFromUrl(fileUrl);
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

}