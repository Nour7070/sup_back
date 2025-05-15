package com.example.supervision.Kafka;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Document;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.classes.UserStatus;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.services.ActivityLogService;
import com.example.supervision.services.SupervisionFileService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.example.supervision.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaConsumerService {

    private final FormateurRepository formateurRepository;
    private final ApprenantRepository apprenantRepository;
    private final UserRepository userRepository ;
    private final ActivityLogService activityLogService ;
    private final CourseRepository courseRepository ;
    private final ObjectMapper objectMapper ;
  
    public KafkaConsumerService(
            FormateurRepository formateurRepository,
            ApprenantRepository apprenantRepository,
            SupervisionFileService fileService , 
            UserRepository userRepository , 
            ActivityLogService activityLogService ,
            CourseRepository courseRepository , 
            ObjectMapper objectMapper) {
        this.formateurRepository = formateurRepository;
        this.apprenantRepository = apprenantRepository;
        this.userRepository = userRepository ;
        this.activityLogService = activityLogService ;  
        this.courseRepository = courseRepository ; 
        this.objectMapper = objectMapper ;}

    /*@KafkaListener(topics = "formateur-pending-topic", groupId = "supervision-service-group")
    public void handleFormateurPending(String jsonPayload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> formateurData = objectMapper.readValue(jsonPayload, Map.class);
            
            System.out.println("Réception d'un nouveau formateur en attente: " + formateurData.get("email"));
            
            String email = (String) formateurData.get("email");
            String prenom = (String) formateurData.get("prenom");
            String nom = (String) formateurData.get("nom");
            String status = (String) formateurData.get("status");
            
            List<String> certificatsUrls = (List<String>) formateurData.get("certificats");
            List<String> experiencesUrls = (List<String>) formateurData.get("experiences");
            
            Optional<Formateur> existingFormateur = formateurRepository.findByEmail(email);
            Formateur formateur;
            
            if (existingFormateur.isPresent()) {
                formateur = existingFormateur.get();
                System.out.println("Mise à jour du formateur existant: " + email);
                formateur.getDocuments().clear();
            } else {
                formateur = new Formateur();
                formateur.setEmail(email);
                System.out.println("Création d'un nouveau formateur: " + email);
            }
            
            formateur.setPrenom(prenom);
            formateur.setNom(nom);
            formateur.setStatus(UserStatus.valueOf(status));
            
            List<Document> documents = new ArrayList<>();
            
            for (String url : certificatsUrls) {
                Document doc = new Document("Certificat - " + email, url, "CERTIFICAT");
                doc.setFormateur(formateur);
                documents.add(doc);
            }
            
            for (String url : experiencesUrls) {
                Document doc = new Document("Expérience - " + email, url, "EXPERIENCE");
                doc.setFormateur(formateur);
                documents.add(doc);
            }
            
            formateur.setDocuments(documents);
            
            formateurRepository.save(formateur);
            System.out.println("Formateur en attente sauvegardé avec succès: " + email);
            // hna nzid activities
            
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement du formateur en attente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */

    @KafkaListener(topics = "formateur-pending-topic", groupId = "supervision-service-group")
public void handleFormateurPending(String jsonPayload) {
    try {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> formateurData = objectMapper.readValue(jsonPayload, Map.class);
        
        // Extraction des données
        String email = (String) formateurData.get("email");
        String prenom = (String) formateurData.get("prenom");
        String nom = (String) formateurData.get("nom");
        String status = (String) formateurData.get("status");
        
        List<String> certificatsUrls = (List<String>) formateurData.get("certificats");
        List<String> experiencesUrls = (List<String>) formateurData.get("experiences");
        
        // Création ou mise à jour du formateur
        Optional<Formateur> existingFormateur = formateurRepository.findByEmail(email);
        Formateur formateur;
        
        if (existingFormateur.isPresent()) {
            formateur = existingFormateur.get();
            System.out.println("Mise à jour du formateur existant: " + email);
            formateur.getDocuments().clear();
        } else {
            formateur = new Formateur();
            formateur.setEmail(email);
            System.out.println("Création d'un nouveau formateur: " + email);
        }
        
        formateur.setPrenom(prenom);
        formateur.setNom(nom);
        formateur.setStatus(UserStatus.valueOf(status));
        
        List<Document> documents = new ArrayList<>();
        
        // Traitement des certificats - notez qu'on utilise simplement les fileName tels quels
        if (certificatsUrls != null) {
            for (String fileName : certificatsUrls) {
                Document doc = new Document();
                doc.setTitle("Certificat - " + extractFileDisplayName(fileName));
                doc.setFileUrl(fileName); // Stockez juste le nom du fichier
                doc.setDocumentType("CERTIFICAT");
                doc.setFormateur(formateur);
                documents.add(doc);
                System.out.println("Ajout du certificat: " + fileName);
            }
        }
        
        // Traitement des expériences
        if (experiencesUrls != null) {
            for (String fileName : experiencesUrls) {
                Document doc = new Document();
                doc.setTitle("Expérience - " + extractFileDisplayName(fileName));
                doc.setFileUrl(fileName); // Stockez juste le nom du fichier
                doc.setDocumentType("EXPERIENCE");
                doc.setFormateur(formateur);
                documents.add(doc);
                System.out.println("Ajout de l'expérience: " + fileName);
            }
        }
        
        formateur.setDocuments(documents);
        formateurRepository.save(formateur);
        System.out.println("Formateur en attente sauvegardé avec succès: " + email);
        String description = String.format("Trainer %s created and pending approval.", formateur.getNom());
        activityLogService.log(
                "NEW_TRAINER_TO_VALIDATE", 
                description, 
                formateur.getId(),
                null,  
                null  
            );
        
    } catch (Exception e) {
        System.out.println("Erreur lors du traitement du formateur en attente: " + e.getMessage());
        e.printStackTrace();
    }
}

// Méthode pour extraire un nom affichable à partir du nom de fichier
private String extractFileDisplayName(String fileName) {
    // Retirer l'UUID si présent (format: uuid_nomfichieroriginal.ext)
    if (fileName.contains("_")) {
        return fileName.substring(fileName.indexOf("_") + 1);
    }
    return fileName;
}
    @KafkaListener(topics = "user-register-topic", groupId = "supervision-service-group")
    public void handleUserRegistration(String jsonPayload) {
        try {
            // Désérialiser la chaîne JSON en Map
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> apprenantData = objectMapper.readValue(jsonPayload, Map.class);
            
            System.out.println("Réception d'un nouveau apprenant " + apprenantData.get("email"));
            
            String email = (String) apprenantData.get("email");
            String prenom = (String) apprenantData.get("prenom");
            String nom = (String) apprenantData.get("nom");
            String username = (String) apprenantData.get("username");
            
            Optional<Apprenant> existingApprenant = apprenantRepository.findByEmail(email);
            Apprenant apprenant;
            
            if (existingApprenant.isPresent()) {
                apprenant = existingApprenant.get();
                System.out.println("Mise à jour de l'apprenant existant: " + email);
            } else {
                apprenant = new Apprenant();
                apprenant.setEmail(email);
                System.out.println("Création d'un nouveau apprenant: " + email);
            }
            
            apprenant.setPrenom(prenom);
            apprenant.setNom(nom);
            apprenant.setUsername(username);
           
            apprenantRepository.save(apprenant);
            System.out.println("Apprenant sauvegardé avec succès: " + email);
            String description = String.format("Student %s signed up .", apprenant.getNom());
            activityLogService.log(
                "NEW_STUDENT_SIGNUP", 
                description, 
                apprenant.getId(),
                null,  
                null  
            );
            
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement de l'apprenant " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @KafkaListener(topics = "user-login-topic", groupId = "Coursee-group")
    public void receiveUserData(String jsonPayload) {
    try {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> userData = objectMapper.readValue(jsonPayload, Map.class);
        
        System.out.println("Données reçues de Kafka : " + userData);

        String email = userData.get("email");
        String newUserType = userData.get("userType");

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            if (!user.getUserType().equals(newUserType)) {
                user.setUserType(newUserType);
                userRepository.save(user);
                System.out.println("Type d'utilisateur mis à jour : " + email + " -> " + newUserType);
            } else {
                System.out.println("L'utilisateur existe déjà avec le même type : " + email);
            }
            return;
        }

        User newUser = new User();
        newUser.setUsername(userData.get("username"));
        newUser.setEmail(email);
        newUser.setPhoto(userData.get("photo"));
        newUser.setUserType(newUserType);

        userRepository.save(newUser);
        System.out.println("Nouvel utilisateur enregistré avec succès !");
    } catch (Exception e) {
        System.out.println("Erreur lors du traitement des données utilisateur: " + e.getMessage());
        e.printStackTrace();
    }
}

@KafkaListener(topics = "course-created-topic", groupId = "supervision-service-group")
    public void handleCourseCreated(String jsonPayload) {
        try {
            Map<String, Object> courseData = objectMapper.readValue(jsonPayload, Map.class);
            
            // Extraction des données de base
            UUID courseId = UUID.fromString((String) courseData.get("courseId"));
            String title = (String) courseData.get("title");
            String description = (String) courseData.get("description");
            String langue = (String) courseData.get("langue");
            String domaine = (String) courseData.get("domaine");
            String formateurEmail = (String) courseData.get("formateurEmail");
            String status = (String) courseData.get("status");
            
            // Création ou mise à jour du cours
            Optional<Cours> existingCourse = courseRepository.findById(courseId);
            Cours course;
            
            if (existingCourse.isPresent()) {
                course = existingCourse.get();
                System.out.println("Mise à jour du cours existant: " + courseId);
            } else {
                course = new Cours();
                course.setId(courseId);
                System.out.println("Création d'un nouveau cours: " + courseId);
            }
            
            course.setTitre(title);
            course.setDescription(description);
            course.setLangue(langue);
            course.setDomaine(domaine);
            course.setFormateurId(formateurEmail);
            course.setStatus(CourseStatus.valueOf(status));
            
            // Traitement des chapitres (s'ils sont inclus dans le message)
            if (courseData.containsKey("chapters")) {
                List<Map<String, Object>> chaptersData = (List<Map<String, Object>>) courseData.get("chapters");
                List<Chapter> chapters = new ArrayList<>();
                
                for (Map<String, Object> chapterData : chaptersData) {
                    Chapter chapter = new Chapter();
                    chapter.setId(UUID.fromString((String) chapterData.get("chapterId")));
                    chapter.setTitle((String) chapterData.get("title"));
                    chapter.setFileUrl((String) chapterData.get("fileUrl"));
                    chapter.setCourse(course);
                    chapters.add(chapter);
                    System.out.println("Ajout du chapitre: " + chapter.getTitle());
                }
                
                course.setChapters(chapters);
            }
            
            courseRepository.save(course);
            System.out.println("Cours sauvegardé avec succès: " + courseId);
            
            // Log d'activité
            String logDescription = String.format("Course %s created and pending approval. Domaine: %s", 
                                                title, domaine);
            activityLogService.log(
                "NEW_COURSE_TO_VALIDATE", 
                logDescription, 
                courseId,
                formateurEmail,
                null
            );
            
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
