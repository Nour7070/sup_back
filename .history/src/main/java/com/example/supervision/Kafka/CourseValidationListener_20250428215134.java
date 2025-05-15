package com.example.supervision.Kafka;

import com.example.coursapp.entities.Course;
import com.example.coursapp.entities.CourseStatus;
import com.example.coursapp.entities.Langue;
import com.example.coursapp.entities.User;
import com.example.coursapp.entities.Chapter;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.ChapterRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                                 FileContentService fileContentService,
                                 @Value("${file.upload-dir}") String uploadDir) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.fileService = fileService;
        this.fileContentService = fileContentService;
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.storageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Erreur création répertoire: " + uploadDir, ex);
        }
    }

    @KafkaListener(topics = "course-pending-topic", groupId = "supervision-group")
    public void handleCourseValidation(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> courseData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            
            Course course = processCourse(courseData);
            
            if (courseData.containsKey("chapters")) {
                processChapters(course, (List<Map<String, Object>>) courseData.get("chapters"));
            }
            
            System.out.println("Cours validé avec succès - ID: " + course.getId());
            
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement Kafka: " + e.getMessage());
            throw new RuntimeException("Échec du traitement du cours", e);
        }
    }

    private Course processCourse(Map<String, Object> courseData) {
        Course course = new Course();
        // Validation des champs obligatoires
        if (!courseData.containsKey("courseId") || !courseData.containsKey("formateurId")) {
            throw new IllegalArgumentException("Données du cours incomplètes");
        }
        
        course.setId(UUID.fromString((String) courseData.get("courseId")));
        course.setTitre((String) courseData.get("titre"));
        course.setDescription((String) courseData.get("description"));
        course.setLangue(Langue.valueOf(((String) courseData.get("langue")).toUpperCase()));
        course.setDomaine((String) courseData.get("domaine"));
        course.setStatus(CourseStatus.PENDING);
        
        User formateur = new User();
        formateur.setId(UUID.fromString((String) courseData.get("formateurId")));
        course.setFormateur(formateur);
        
        return courseRepository.save(course);
    }

    private void processChapters(Course course, List<Map<String, Object>> chaptersData) {
        if (chaptersData == null || chaptersData.isEmpty()) return;
        
        for (Map<String, Object> chapterData : chaptersData) {
            Chapter chapter = new Chapter();
            chapter.setTitle((String) chapterData.get("title"));
            chapter.setDescription((String) chapterData.get("description"));
            chapter.setCourse(course);
            
            processChapterFile(chapter, (String) chapterData.get("fileUrl"));
            
            chapterRepository.save(chapter);
        }
    }

    private void processChapterFile(Chapter chapter, String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        
        try {
            Resource fileResource = fileService.loadFileFromUrl(fileUrl);
            String originalFileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            String storedFileName = "chapter_" + UUID.randomUUID() + "_" + originalFileName;
            Path targetLocation = storageLocation.resolve(storedFileName);
            
            Files.copy(fileResource.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            chapter.setFileUrl(storedFileName);
            
            if (storedFileName.toLowerCase().matches(".*\\.(pdf|pptx)$")) {
                String content = fileContentService.extractContentFromStoredFile(storedFileName);
                // Traitement supplémentaire du contenu si nécessaire
            }
            
        } catch (Exception e) {
            System.err.println("Échec du traitement du fichier: " + fileUrl + " - " + e.getMessage());
            // Poursuivre sans le fichier
        }
    }
}