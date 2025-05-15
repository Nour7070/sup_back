package com.example.supervision.Kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Document;
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

    @Autowired
    public KafkaConsumerService(
            FormateurRepository formateurRepository,
            ApprenantRepository apprenantRepository,
            SupervisionFileService fileService) {
        this.formateurRepository = formateurRepository;
        this.apprenantRepository = apprenantRepository;
    }

    @KafkaListener(topics = "formateur-pending-topic", groupId = "supervision-service-group")
    public void handleFormateurPending(Formateur formateur) {
        try {
            System.out.println("Réception d'un nouveau formateur en attente: " + formateur.getEmail());
    
            String email = formateur.getEmail();
            String prenom = formateur.getFirstName();
            String nom = formateur.getLastName();
            String status = formateur.getStatus().name();
    
            List<String> certificatsUrls = formateur.getCertificats();
            List<String> experiencesUrls = formateur.getExperiences();
    
            Optional<Formateur> existingFormateur = formateurRepository.findByEmail(email);
            Formateur formateurToSave;
    
            if (existingFormateur.isPresent()) {
                formateurToSave = existingFormateur.get();
                System.out.println("Mise à jour du formateur existant: " + email);
                // Supprimer les anciens documents liés (si tu veux les remplacer complètement)
                formateurToSave.getDocuments().clear();
            } else {
                formateurToSave = new Formateur();
                formateurToSave.setEmail(email);
                System.out.println("Création d'un nouveau formateur: " + email);
            }
    
            // Mettre à jour les infos de base
            formateurToSave.setPrenom(prenom);
            formateurToSave.setNom(nom);
            formateurToSave.setStatus(UserStatus.valueOf(status));
    
            // Créer les objets Document
            List<Document> documents = new ArrayList<>();
    
            for (String url : certificatsUrls) {
                Document doc = new Document("Certificat - " + email, url, "CERTIFICAT");
                doc.setFormateur(formateurToSave);
                documents.add(doc);
            }
    
            for (String url : experiencesUrls) {
                Document doc = new Document("Expérience - " + email, url, "EXPERIENCE");
                doc.setFormateur(formateurToSave);
                documents.add(doc);
            }
    
            // Affecter les documents au formateur
            formateurToSave.setDocuments(documents);
    
            // Sauvegarde
            formateurRepository.save(formateurToSave);
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
}