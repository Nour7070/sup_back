package com.example.supervision.Kafka;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.User;
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

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.supervision.services.FileContentService;
import com.example.supervision.services.SupervisionFileService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
public class CourseValidationListener {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileService;
    private final FileContentService fileContentService;

    public CourseValidationListener(CourseRepository courseRepository,
                                  ChapterRepository chapterRepository,
                                  SupervisionFileService fileService,
                                  FileContentService fileContentService) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.fileService = fileService;
        this.fileContentService = fileContentService;
    }

    @KafkaListener(topics = "course-pending-topic", groupId = "supervision-group")
    public void handleCourseValidation(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> courseData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            
            // 1. Traitement du cours
            Cours course = processCourse(courseData);
            
            // 2. Traitement des chapitres et fichiers
            processChapters(course, (List<Map<String, Object>>) courseData.get("chapters"));
            
            System.out.println("Cours {} sauvegardé avec succès" + course.getId());
            
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement du message Kafka" +  e);
            throw new RuntimeException("Erreur de traitement Kafka", e);
        }
    }

    private Cours processCourse(Map<String, Object> courseData) {
        Cours course = new Cours();
        course.setId(UUID.fromString((String) courseData.get("courseId")));
        course.setTitre((String) courseData.get("titre"));
        course.setDescription((String) courseData.get("description"));
        course.setLangue(Langue.valueOf((String) courseData.get("langue")));
        course.setDomaine((String) courseData.get("domaine"));
        course.setStatus(CourseStatus.PENDING);
        
        // Gestion du formateur (simplifiée)
        User formateur = new User();
        formateur.setId(Long.parseLong((String) courseData.get("formateurId")));
        course.setFormateurId(formateurId);
        
        return courseRepository.save(course);
    }

    private void processChapters(Cours course, List<Map<String, Object>> chaptersData) {
        for (Map<String, Object> chapterData : chaptersData) {
            Chapter chapter = new Chapter();
            chapter.setTitle((String) chapterData.get("title"));
            chapter.setDescription((String) chapterData.get("description"));
            chapter.setCours(course);
            
            // Téléchargement et sauvegarde du fichier
            String fileUrl = (String) chapterData.get("fileUrl");
            if (fileUrl != null && !fileUrl.isEmpty()) {
                try {
                    Resource fileResource = fileService.loadFileFromUrl(fileUrl);
                    
                    // Sauvegarde du fichier localement
                    String storedFileName = "chapter_" + chapter.getId() + "_" + fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                    Files.copy(fileResource.getInputStream(), 
                             fileService.getStorageLocation().resolve(storedFileName),
                             StandardCopyOption.REPLACE_EXISTING);
                    
                    chapter.setFileUrl(storedFileName);
                    
                    // Extraction du contenu si nécessaire (PDF/PPTX)
                    if (storedFileName.endsWith(".pdf") || storedFileName.endsWith(".pptx")) {
                        String content = fileContentService.extractContentFromStoredFile(storedFileName);
                        // Vous pourriez stocker le contenu extrait si besoin
                    }
                    
                } catch (Exception e) {
                    System.out.println("Erreur lors du traitement du fichier pour le chapitre" + e);
                    // Continuer sans le fichier mais logger l'erreur
                }
            }
            
            chapterRepository.save(chapter);
        }
    }
}