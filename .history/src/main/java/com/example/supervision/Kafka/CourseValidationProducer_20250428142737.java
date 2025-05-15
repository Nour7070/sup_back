package com.example.supervision.Kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.supervision.classes.CourseStatus;

@Component
public class CourseValidationProducer {

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    
    private static final String COURSE_VALIDATION_RESPONSE_TOPIC = "course-validation-response-topic";

    public CourseValidationProducer (KafkaTemplate kafkaTemplate)
    {
        this.kafkaTemplate = kafkaTemplate ;
    }

    public void sendValidationResult(UUID courseId, CourseStatus status, String message) {
        try {
            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("courseId", courseId);
            validationResult.put("status", status.name());
            validationResult.put("message", message);
            validationResult.put("timestamp", System.currentTimeMillis());
            
            System.out.print("Envoi du résultat de validation pour le cours {}: {}" + courseId + status);
            kafkaTemplate.send(COURSE_VALIDATION_RESPONSE_TOPIC, validationResult);
            System.out.print("Résultat de validation envoyé avec succès pour le cours {}" + courseId);
        } catch (Exception e) {
            System.out.print("Erreur lors de l'envoi du résultat de validation pour le cours {}: {}"+ 
                    courseId + e.getMessage() e);
            throw new RuntimeException("Échec de l'envoi du résultat de validation", e);
        }
    }
}