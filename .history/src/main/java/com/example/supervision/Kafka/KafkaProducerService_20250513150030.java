package com.example.supervision.Kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.supervision.classes.Moderateur;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, 
                              ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /*public void sendFormateurStatus(String email, String status) {
        try {
            String message = objectMapper.writeValueAsString(
                Map.of("email", email, "status", status)
            );
            kafkaTemplate.send("formateur-status", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur Kafka", e);
        }
    }*/

    public void sendFormateurStatus(Long id, String email, String status) {
        try {
            String message = objectMapper.writeValueAsString(
                Map.of("id", id, 
                "email", email, "status", status)
            );
            kafkaTemplate.send("formateur-status", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur Kafka", e);
        }
    }
    

    public void sendModerator(Moderateur moderateur) {
        Map<String, Object> moderateurData = new HashMap<>();
        moderateurData.put("email", moderateur.getEmail());
        moderateurData.put("password", moderateur.getPassword());
        moderateurData.put("username", moderateur.getUsername());
        moderateurData.put("nom", moderateur.getNom());
        moderateurData.put("prenom", moderateur.getPrenom());
        moderateurData.put("phone", moderateur.getPhoneNumber());
        moderateurData.put("photo", moderateur.getPhoto());
        
        System.out.println("Données envoyées au Kafka : " + moderateurData);
        System.out.println("[KafkaProducer] Données AVANT sérialisation : " + moderateurData);
    
        try {
            String jsonMessage = objectMapper.writeValueAsString(moderateurData);
            System.out.println("[KafkaProducer] Message JSON envoyé : " + jsonMessage);
            kafkaTemplate.send("new-moderator", jsonMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur Kafka", e);
        }
    }

    public void sendCoursStatus(UUID id, String status) {
        try {
            String message = objectMapper.writeValueAsString(
                Map.of("courseId", id, "status", status)
            );
            kafkaTemplate.send("cours-status", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur Kafka", e);
        }
    }

}