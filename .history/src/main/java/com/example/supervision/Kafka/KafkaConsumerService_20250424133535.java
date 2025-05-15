package com.example.supervision.Kafka;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.UserStatus;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.services.SupervisionFileService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaConsumerService {

    private final FormateurRepository formateurRepository;
    private final ApprenantRepository apprenantRepository;
    private final SupervisionFileService fileService;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumerService(
            FormateurRepository formateurRepository,
            ApprenantRepository apprenantRepository,
            SupervisionFileService fileService) {
        this.formateurRepository = formateurRepository;
        this.apprenantRepository = apprenantRepository;
        this.fileService = fileService;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "formateur-pending-topic", groupId = "supervision-service-group")
    public void handleFormateurPending(Map<String, Object> formateurData) {
        try {
            System.out.println("Réception d'un nouveau formateur en attente: " + formateurData.get("email"));
            // Extraction des données du formateur
            String email = (String) formateurData.get("email");
            String prenom = (String) formateurData.get("prenom");
            String nom = (String) formateurData.get("nom");
            String status = (String) formateurData.get("status");
            
            // Récupération des URLs des fichiers
            List<String> certificatsUrls = (List<String>) formateurData.get("certificats");
            List<String> experiencesUrls = (List<String>) formateurData.get("experiences");
            
            // Vérification si le formateur existe déjà
            Optional<Formateur> existingFormateur = formateurRepository.findByEmail(email);
            Formateur formateur;
            
            if (existingFormateur.isPresent()) {
                formateur = existingFormateur.get();
                System.out.println("Mise à jour du formateur existant: " + email);
            } else {
                formateur = new Formateur();
                formateur.setEmail(email);
                System.out.println("Création d'un nouveau formateur:" + email);
            }
            
            // Mise à jour des données du formateur
            formateur.setPrenom(prenom);
            formateur.setNom(nom);
            formateur.setStatus(UserStatus.valueOf(status));
            formateur.setCertificats(certificatsUrls);
            formateur.setExperiences(experiencesUrls);
            
            // Sauvegarde du formateur
            formateurRepository.save(formateur);
            log.info("Formateur en attente sauvegardé avec succès: {}", email);
            
            // Ici vous pourriez ajouter une logique pour notifier les superviseurs
            // de la présence d'un nouveau formateur à valider
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement du formateur en attente: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "user-register-topic", groupId = "supervision-service-group")
    public void handleUserRegistration(Map<String, String> userData) {
        try {
            String userType = userData.get("userType");
            
            if ("APPRENANT".equalsIgnoreCase(userType)) {
                handleApprenantRegistration(userData);
            }
            // Vous pourriez ajouter d'autres types d'utilisateurs si nécessaire
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'inscription utilisateur: {}", e.getMessage(), e);
        }
    }
    
    private void handleApprenantRegistration(Map<String, String> userData) {
        String email = userData.get("email");
        String username = userData.get("username");
        String firstName = userData.get("firstName");
        String lastName = userData.get("lastName");
        String photo = userData.get("photo");
        
        log.info("Réception d'un nouvel apprenant: {}", email);
        
        // Vérification si l'apprenant existe déjà
        Optional<Apprenant> existingApprenant = apprenantRepository.findByEmail(email);
        Apprenant apprenant;
        
        if (existingApprenant.isPresent()) {
            apprenant = existingApprenant.get();
            log.info("Mise à jour de l'apprenant existant: {}", email);
        } else {
            apprenant = new Apprenant();
            apprenant.setEmail(email);
            log.info("Création d'un nouvel apprenant: {}", email);
        }
        
        // Mise à jour des données de l'apprenant
        apprenant.setUsername(username);
        apprenant.setPrenom(firstName);
        apprenant.setNom(lastName);
        apprenant.setPhoto(photo);
        
        // Sauvegarde de l'apprenant
        apprenantRepository.save(apprenant);
        log.info("Apprenant sauvegardé avec succès: {}", email);
    }
    
    // Méthode pour envoyer le résultat de validation d'un formateur
    public void sendFormateurValidationResult(String formateurEmail, FormateurStatus status) {
        try {
            Map<String, Object> result = Map.of(
                "formateurEmail", formateurEmail,
                "status", status.name()
            );
            
            // Cette méthode devrait être implémentée dans un service Kafka Producer
            // kafkaProducerService.sendMessage("formateur-validation-result", result);
            
            log.info("Résultat de validation envoyé pour le formateur {}: {}", formateurEmail, status);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du résultat de validation: {}", e.getMessage(), e);
        }
    }
}