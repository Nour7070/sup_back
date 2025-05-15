package com.example.supervision.Kafka;

import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
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

    @KafkaListener(topics = "course-pending-topic", groupId = "course-validation-group")
    @Transactional
    public void listenForCourseValidation(ConsumerRecord<String, String> record) {
        try {
            // Récupérer le message JSON reçu
            String message = record.value();
            System.out.println("[CourseKafkaListener] Message reçu : " + message);

            // Désérialiser les données JSON
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> courseData = objectMapper.readValue(message, Map.class);

            // Extraire les données du cours
            String courseIdStr = (String) courseData.get("courseId");
            UUID courseId = UUID.fromString(courseIdStr);
            String titre = (String) courseData.get("titre");
            String description = (String) courseData.get("description");
            Langue langue = (Langue) courseData.get("langue");
            String domaine = (String) courseData.get("domaine");
            String formateurIdStr = (String) courseData.get("formateurId");
            Long formateurId = Long.valueOf(formateurIdStr);

            // Vérifier que le cours existe
            Cours course;
if (courseRepository.existsById(courseId)) {
    course = courseRepository.findById(courseId).get();
} else {
    // Créer un nouveau cours
    course = new Cours();
    course.setId(courseId);
    course.setTitre(titre);
    course.setDescription(description);
    course.setLangue(langue);
    course.setDomaine(domaine);
    course.setFormateurId(formateurId);
    // Définir d'autres champs nécessaires...
    
    // Sauvegarder le nouveau cours
    course = courseRepository.save(course);
}

            // Extraire les chapitres du cours
            List<Map<String, Object>> chaptersData = (List<Map<String, Object>>) courseData.get("chapters");
            for (Map<String, Object> chapterData : chaptersData) {
                String chapterTitle = (String) chapterData.get("title");
                String chapterDescription = (String) chapterData.get("description");
                String fileUrl = (String) chapterData.get("fileUrl");

                // Extraire le nom du fichier depuis l'URL
                String fileName = fileUrl.replace("file://", "");

                // Créer ou mettre à jour le chapitre sans validation
                Chapter chapter = new Chapter();
                chapter.setTitle(chapterTitle);
                chapter.setDescription(chapterDescription);
                chapter.setFileUrl(fileName);
                chapter.setCours(course);  // Associer le chapitre au cours

                // Sauvegarder le chapitre
                chapterRepository.save(chapter);
            }

            // Sauvegarder le cours après mise à jour
            courseRepository.save(course);

            System.out.println("[CourseKafkaListener] Cours récupéré : " + titre);

        } catch (Exception e) {
            System.err.println("[CourseKafkaListener] Erreur lors du traitement du cours : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
