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

     public void sendFormateurPending(Moderateur moderateur) {
        Map<String, Object> moderateurData = new HashMap<>();
        moderateurData.put("email", formateur.getEmail());
        formateurData.put("prenom", formateur.getFirstName());
        formateurData.put("nom", formateur.getLastName());
        formateurData.put("username", formateur.getUsername());
        formateurData.put("status", formateur.getStatus().name());
        formateurData.put("certificats", formateur.getCertificats());
        formateurData.put("experiences", formateur.getExperiences());
    
        kafkaTemplate.send("formateur-pending-topic", formateurData);
    }

}