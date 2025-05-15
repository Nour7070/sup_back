/*package com.example.supervision.Kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Object message) {
        System.out.println("Envoi d'un message Kafka au topic" + topic + message);
        kafkaTemplate.send(topic, message);
    }
}*/

package com.example.supervision.Kafka;

import java.util.HashMap;
import java.util.Map;
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

    public void sendFormateurStatus(String email, String status) {
        try {
            String message = objectMapper.writeValueAsString(
                Map.of("email", email, "status", status)
            );
            kafkaTemplate.send("formateur-status", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur Kafka", e);
        }
    }

   public void sendModeratorData(Moderateur moderateur) {
        Map<String, Object> moderateurData = new HashMap<>();
        moderateurData.put("email", moderateur.getEmail());
        moderateurData.put("password", moderateur.getPassword());
        moderateurData.put("username", moderateur.getUsername());
        moderateurData.put("nom", moderateur.getNom());
        moderateurData.put("prenom", moderateur.getPrenom());
        moderateurData.put("phone", moderateur.getPhoneNumber());
        moderateurData.put("photo", moderateur.getPhoto());
        
        System.out.println("Envoi des données du modérateur au service d'auth via Kafka:" + moderateur.getEmail());
        
        kafkaTemplate.send(TOPIC, moderateur.getEmail(), moderateurData)
            .addCallback(
                result -> System.out.println("Message envoyé avec succès pour : {}", moderateur.getEmail()),
                ex -> logger.error("Échec de l'envoi du message pour : {}", moderateur.getEmail(), ex)
            );
    }
}