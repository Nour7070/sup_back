import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CourseKafkaListener {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final FileStorageService fileStorageService;

    public CourseKafkaListener(CourseRepository courseRepository,
                               ChapterRepository chapterRepository,
                               FileStorageService fileStorageService) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.fileStorageService = fileStorageService;
    }

    @KafkaListener(topics = "course-pending-topic", groupId = "course-validation-group")
    @Transactional
    public void listenForCourseValidation(ConsumerRecord<String, String> record) {
        try {
            // Récupérer le message JSON reçu
            String message = record.value();
            System.out.println("[CourseKafkaListener] Message reçu : " + message);

            // Désérialiser les données JSON
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> courseData = objectMapper.readValue(message, Map.class);

            // Extraire les données du cours
            String courseIdStr = (String) courseData.get("courseId");
            UUID courseId = UUID.fromString(courseIdStr);
            String titre = (String) courseData.get("titre");
            String description = (String) courseData.get("description");
            String langue = (String) courseData.get("langue");
            String domaine = (String) courseData.get("domaine");
            String formateurIdStr = (String) courseData.get("formateurId");
            Long formateurId = Long.valueOf(formateurIdStr);

            // Vérifier que le cours existe
            Cours course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé pour l'ID : " + courseId));

            // Extraire les chapitres du cours
            List<Map<String, Object>> chaptersData = (List<Map<String, Object>>) courseData.get("chapters");
            for (Map<String, Object> chapterData : chaptersData) {
                String chapterTitle = (String) chapterData.get("title");
                String chapterDescription = (String) chapterData.get("description");
                String fileUrl = (String) chapterData.get("fileUrl");

                // Extraire le nom du fichier depuis l'URL
                String fileName = fileUrl.replace("file://", "");

                // Créer ou mettre à jour le chapitre sans validation
                Chapter chapter = new Chapter();
                chapter.setTitre(chapterTitle);
                chapter.setDescription(chapterDescription);
                chapter.setFileUrl(fileName);
                chapter.setCours(course);  // Associer le chapitre au cours

                // Sauvegarder le chapitre
                chapterRepository.save(chapter);
            }

            // Sauvegarder le cours après mise à jour
            courseRepository.save(course);

            System.out.println("[CourseKafkaListener] Cours récupéré : " + titre);

        } catch (Exception e) {
            System.err.println("[CourseKafkaListener] Erreur lors du traitement du cours : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
