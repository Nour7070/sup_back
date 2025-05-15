package com.example.supervision.Kafka;

import com.example.supervision.entities.Course;
import com.example.supervision.entities.CourseStatus;
import com.example.supervision.entities.Langue;
import com.example.supervision.entities.User;
import com.example.supervision.entities.Chapter;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.ChapterRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.supervision.services.FileContentService;
import com.example.supervision.services.SupervisionFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class CourseValidationListener {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileService;
    private final FileContentService fileContentService;
    private final Path storageLocation;

    public CourseValidationListener(CourseRepository courseRepository,
                                  ChapterRepository chapterRepository,
                                  SupervisionFileService fileService,
                                  FileContentService fileContentService) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.fileService = fileService;
        this.fileContentService = fileContentService;
        this.storageLocation = fileService.getStorageLocation();
    }

    @KafkaListener(topics = "course-pending-topic", groupId = "supervision-group")
    public void handleCourseValidation(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> courseData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            
            // 1. Traitement du cours
            Course course = processCourse(courseData);
            
            // 2. Traitement des chapitres et fichiers
            processChapters(course, (List<Map<String, Object>>) courseData.get("chapters"));
            
            System.out.println("Cours " + course.getId() + " sauvegardé avec succès");
            
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement du message Kafka: " + e);
            throw new RuntimeException("Erreur de traitement Kafka", e);
        }
    }

    private Course processCourse(Map<String, Object> courseData) {
        Course course = new Course();
        course.setId(UUID.fromString((String) courseData.get("courseId")));
        course.setTitre((String) courseData.get("titre"));
        course.setDescription((String) courseData.get("description"));
        course.setLangue(Langue.valueOf((String) courseData.get("langue")));
        course.setDomaine((String) courseData.get("domaine"));
        course.setStatus(CourseStatus.PENDING);
        
        // Gestion du formateur
        User formateur = new User();
        formateur.setId(UUID.fromString((String) courseData.get("formateurId")));
        course.setFormateur(formateur);
        
        return courseRepository.save(course);
    }

    private void processChapters(Course course, List<Map<String, Object>> chaptersData) {
        for (Map<String, Object> chapterData : chaptersData) {
            Chapter chapter = new Chapter();
            chapter.setTitle((String) chapterData.get("title"));
            chapter.setDescription((String) chapterData.get("description"));
            chapter.setCourse(course);
            
            // Téléchargement et sauvegarde du fichier
            String fileUrl = (String) chapterData.get("fileUrl");
            if (fileUrl != null && !fileUrl.isEmpty()) {
                try {
                    Resource fileResource = fileService.loadFileFromUrl(fileUrl);
                    
                    // Sauvegarde du fichier localement
                    String storedFileName = "chapter_" + UUID.randomUUID() + "_" + fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                    Files.copy(fileResource.getInputStream(), 
                             storageLocation.resolve(storedFileName),
                             StandardCopyOption.REPLACE_EXISTING);
                    
                    chapter.setFileUrl(storedFileName);
                    
                    // Extraction du contenu si nécessaire (PDF/PPTX)
                    if (storedFileName.endsWith(".pdf") || storedFileName.endsWith(".pptx")) {
                        String content = fileContentService.extractContentFromStoredFile(storedFileName);
                        // Vous pourriez stocker le contenu extrait si besoin
                    }
                    
                } catch (Exception e) {
                    System.out.println("Erreur lors du traitement du fichier pour le chapitre: " + e);
                    // Continuer sans le fichier mais logger l'erreur
                }
            }
            
            chapterRepository.save(chapter);
        }
    }
}