package com.example.supervision.Kafka;

import java.util.ArrayList;
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
            // Supprimer les anciens documents liés (si tu veux les remplacer complètement)
            formateur.getDocuments().clear();
        } else {
            formateur = new Formateur();
            formateur.setEmail(email);
            System.out.println("Création d'un nouveau formateur: " + email);
        }

        // Mettre à jour les infos de base
        formateur.setPrenom(prenom);
        formateur.setNom(nom);
        formateur.setStatus(UserStatus.valueOf(status));

        // Créer les objets Document
        List<Document> documents = new ArrayList<>();

        for (String url : certificatsUrls) {
            Document doc = new Document("Certificat - " + email, url, "CERTIFICAT");
            doc.setFormateur(formateur); // lien bidirectionnel
            documents.add(doc);
        }

        for (String url : experiencesUrls) {
            Document doc = new Document("Expérience - " + email, url, "EXPERIENCE");
            doc.setFormateur(formateur);
            documents.add(doc);
        }

        // Affecter les documents au formateur
        formateur.setDocuments(documents);

        // Sauvegarde
        formateurRepository.save(formateur);
        System.out.println("Formateur en attente sauvegardé avec succès: " + email);


    } catch (Exception e) {
        System.out.println("Erreur lors du traitement du formateur en attente: " + e.getMessage());
        e.printStackTrace();
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
            System.out.println("Erreur lors du traitement de l'inscription utilisateur: " + e.getMessage() + e);
        }
    }
    
    private void handleApprenantRegistration(Map<String, String> userData) {
        String email = userData.get("email");
        String username = userData.get("username");
        String firstName = userData.get("firstName");
        String lastName = userData.get("lastName");
        String photo = userData.get("photo");
        
        System.out.println("Réception d'un nouvel apprenant: " + email);
        
        // Vérification si l'apprenant existe déjà
        Optional<Apprenant> existingApprenant = apprenantRepository.findByEmail(email);
        Apprenant apprenant;
        
        if (existingApprenant.isPresent()) {
            apprenant = existingApprenant.get();
            System.out.println("Mise à jour de l'apprenant existant: " + email);
        } else {
            apprenant = new Apprenant();
            apprenant.setEmail(email);
            System.out.println("Création d'un nouvel apprenant: " + email);
        }
        
        // Mise à jour des données de l'apprenant
        apprenant.setUsername(username);
        apprenant.setPrenom(firstName);
        apprenant.setNom(lastName);
        apprenant.setPhoto(photo);
        
        // Sauvegarde de l'apprenant
        apprenantRepository.save(apprenant);
        System.out.println("Apprenant sauvegardé avec succès: " + email);
    }
    
    // Méthode pour envoyer le résultat de validation d'un formateur
    public void sendFormateurValidationResult(String formateurEmail, UserStatus status) {
        try {
            Map<String, Object> result = Map.of(
                "formateurEmail", formateurEmail,
                "status", status.name()
            );
            
            // Cette méthode devrait être implémentée dans un service Kafka Producer
            // kafkaProducerService.sendMessage("formateur-validation-result", result);
            
            System.out.println("Résultat de validation envoyé pour le formateur : " + formateurEmail + status);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi du résultat de validation: " + e.getMessage() + e);
        }
    }
}