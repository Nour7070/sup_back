package com.example.supervision.Kafka;

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
