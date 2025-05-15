package com.example.supervision;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class CourseValidationKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Producer pour envoyer des Map<String, Object>
    @Bean
    public ProducerFactory<String, Map<String, Object>> courseValidationProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Map<String, Object>> courseValidationKafkaTemplate() {
        return new KafkaTemplate<>(courseValidationProducerFactory());
    }

    // Consumer pour recevoir des Map<String, Object>
    @Bean
    public ConsumerFactory<String, Map<String, Object>> courseValidationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "supervision-service-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
public ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> courseValidationKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> factory =
             new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(courseValidationConsumerFactory());
    
    // Ajout d'un error handler pour débogage
    factory.setErrorHandler((exception, data) -> {
        System.err.println("Erreur lors du traitement du message Kafka: " + exception.getMessage());
        System.err.println("Données concernées: " + data);
        exception.printStackTrace();
    });
    
    return factory;
}
Problèmes de compatibilité Producer/Consumer confirmés
Après avoir analysé votre configuration et le code du producer et consumer, je confirme les problèmes suivants:

Incompatibilité de sérialisation/désérialisation:
Le producer utilise JsonSerializer mais n'inclut pas d'information de type
Le consumer attend une structure Map spécifique mais sans type explicite
Incompatibilité dans les noms des champs:
Producer: chapter.getTitre() → stocké comme "titre"
Consumer: recherche chapterMap.get("title")
Traitement du courseId:
Le producer envoie probablement un UUID
Le consumer tente de récupérer une String puis de la reconvertir en UUID
Solution globale recommandée
Standardisez les noms de champs entre les services:
java
// Dans le producer
chapterData.put("title", chapter.getTitre()); // Utiliser "title" au lieu de "titre"
chapterData.put("description", chapter.getDescription());
Assurez-vous que le courseId est envoyé sous forme de String:
java
// Dans le producer
courseData.put("courseId", courseId.toString()); // Convertir UUID en String
Ajoutez des logs détaillés pour le débogage:
Dans le producer, loggez la structure exacte des données envoyées
Dans le consumer, loggez les données reçues avant traitement
Testez avec un message simple: Créez un consumer de test simple qui se contente de logger le message reçu pour confirmer la réception
Vérifiez les logs Kafka pour confirmer que les messages sont bien envoyés et reçus
Le problème principal semble être l'incompatibilité entre les noms de propriétés utilisés dans vos deux services. Standardisez ces noms et améliorez la configuration de désérialisation pour résoudre le problème.






    // Producer pour envoyer des String (si besoin ailleurs dans l'app)
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }
}
