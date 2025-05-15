package com.example.supervision.Kafka;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.Document;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.Langue;
import com.example.supervision.classes.User;
import com.example.supervision.classes.UserStatus;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.services.ActivityLogService;
import com.example.supervision.services.SupervisionFileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;


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
    private final ChapterRepository chapterRepository ;
    private final CourseRepository coursRepository ;
  
    public KafkaConsumerService(
            FormateurRepository formateurRepository,
            ApprenantRepository apprenantRepository,
            SupervisionFileService fileService , 
            UserRepository userRepository , 
            ActivityLogService activityLogService ,
            CourseRepository courseRepository , 
            ObjectMapper objectMapper ,
            ChapterRepository chapterRepository ,
            CourseRepository coursRepository) {
        this.formateurRepository = formateurRepository;
        this.apprenantRepository = apprenantRepository;
        this.userRepository = userRepository ;
        this.activityLogService = activityLogService ;  
        this.courseRepository = courseRepository ; 
        this.objectMapper = objectMapper ;
        this.chapterRepository = chapterRepository ;
        this.coursRepository = coursRepository ;}

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
@Transactional
public void handleCourseCreated(String message) {
    try {
        Map<String, Object> data = objectMapper.readValue(message, Map.class);
        
        UUID courseId = UUID.fromString((String) data.get("courseId"));
        String title = (String) data.get("title");
        String description = (String) data.get("description");
        String langueStr = (String) data.get("langue");
        String domaine = (String) data.get("domaine");
        Long formateurId = ((Number) data.get("formateurId")).longValue(); 
        
        // Conversion de la langue
        Langue langue = Langue.FRANCAIS;
        if (langueStr != null) {
            try {
                langue = Langue.valueOf(langueStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Langue invalide: {}, défaut FRENCH utilisé" + langueStr);
            }
        }
        
        // Création de l'entité Cours
        Cours cours = new Cours();
        cours.setId(courseId);
        cours.setTitre(title);
        cours.setDescription(description);
        cours.setLangue(langue);
        cours.setDomaine(domaine);
        cours.setFormateurId(formateurId); 
        
        coursRepository.save(cours);
        
    } catch (Exception e) {
        System.out.println("Erreur lors du traitement du cours" + e);
        throw new RuntimeException("Erreur Kafka" + e);
    }
}


@KafkaListener(topics = "chapter-created-topic", groupId = "supervision-service-group")
public void handleChapterCreated(String message) {
    try {
        // 1. Désérialisation du message
        Map<String, String> data = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});
        
        // 2. Validation des données obligatoires
        if (data.get("chapterId") == null || data.get("courseId") == null || data.get("fileUrl") == null) {
            throw new IllegalArgumentException("Message Kafka invalide - champs manquants");
        }

        // 3. Création du chapitre
        Chapter chapter = new Chapter();
        chapter.setId(UUID.fromString(data.get("chapterId")));
        chapter.setTitle(data.get("title")); // Optionnel si le titre peut être null
        chapter.setFileUrl(data.get("fileUrl"));

        // 4. Association au cours
        UUID courseId = UUID.fromString(data.get("courseId"));
        Cours course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Cours introuvable: " + courseId));
        
        chapter.setCours(course);
        
        // 5. Sauvegarde
        chapterRepository.save(chapter);
        
        System.out.println("Chapitre {} créé pour le cours {}" + chapter.getId() + courseId);
        String description = String.format("Student %s signed up .", chapter.getTitle());
            activityLogService.log(
                "NEW_STUDENT_SIGNUP", 
                description, 
                chapter.getId(),
                null,  
                null  
            );

    } catch (JsonProcessingException e) {
        System.out.println("Erreur de désérialisation du message Kafka: {}" + message + e);
        // Vous pourriez aussi envoyer le message vers une Dead Letter Queue ici
    } catch (IllegalArgumentException e) {
        System.out.println("Données du chapitre invalides: {}" + e.getMessage());
    } catch (Exception e) {
        System.out.println("Erreur inattendue lors du traitement du chapitre" + e);
    }
}
}
