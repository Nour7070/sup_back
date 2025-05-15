package com.example.supervision.Kafka;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Langue;
import com.example.supervision.classes.User;
import com.example.supervision.classes.Chapter;
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

import jakarta.transaction.Transactional;

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

    @Transactional
    @KafkaListener(topics = "course-pending-topic", groupId = "supervision-group")
    public void handleCourseValidation(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> courseData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            
            Cours course = processCourse(courseData);
            
            if (courseData.containsKey("chapters")) {
                processChapters(course, (List<Map<String, Object>>) courseData.get("chapters"));
            }
            
            System.out.println("Cours validé avec succès - ID: " + course.getId());
            
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement Kafka: " + e.getMessage());
            throw new RuntimeException("Échec du traitement du cours", e);
        }
    }


    private Cours processCourse(Map<String, Object> courseData) {
        Cours cours = new Cours();
        // Validation des champs obligatoires
        if (!courseData.containsKey("courseId") || !courseData.containsKey("formateurId")) {
            throw new IllegalArgumentException("Données du cours incomplètes");
        }
        
        // Conversion de l'UUID en String pour le cours
        cours.setId(UUID.fromString((String) courseData.get("courseId")));
        cours.setTitre((String) courseData.get("titre"));
        cours.setDescription((String) courseData.get("description"));
        cours.setLangue(Langue.valueOf(((String) courseData.get("langue")).toUpperCase()));
        cours.setDomaine((String) courseData.get("domaine"));
        cours.setStatus(CourseStatus.PENDING);
        
        // Création du formateur avec ID Long
        User formateur = new User();
        // Conversion de l'UUID en Long pour le formateur
        String formateurIdStr = ((String) courseData.get("formateurId")).replace("-", "");
        Long formateurId = Long.parseLong(formateurIdStr.substring(0, Math.min(formateurIdStr.length(), 18)), 16);
        formateur.setId(formateurId);
        
        // Selon votre implémentation de Cours
        cours.setFormateurId(formateur.getId()); // Si vous avez setFormateurId(Long)
        // OU
        // cours.setFormateur(formateur); // Si vous avez setFormateur(User)
        
        return courseRepository.save(cours);
    }

    private void processChapters(Cours course, List<Map<String, Object>> chaptersData) {
        if (chaptersData == null || chaptersData.isEmpty()) return;
        
        for (Map<String, Object> chapterData : chaptersData) {
            Chapter chapter = new Chapter();
            chapter.setTitle((String) chapterData.get("title"));
            chapter.setDescription((String) chapterData.get("description"));
            chapter.setCours(course);
            
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