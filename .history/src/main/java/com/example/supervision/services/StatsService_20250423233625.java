package com.example.supervision.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Formateur;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.repositories.FormateurSubscriptionRepository;

@Service
public class StatsService {

   
@Autowired
private FormateurRepository formateurRepository;

@Autowired
private CourseRepository courseRepository;

@Autowired
private FormateurSubscriptionRepository subscriptionRepository ;


public Map<String, Object> getFormateurStats(Long formateurId) {
    Map<String, Object> stats = new HashMap<>();
    
    Formateur formateur = formateurRepository.findById(formateurId)
        .orElseThrow(() -> new ResourceNotFoundException("Formateur not found"));
    
    stats.put("name", formateur.getNom() + " " + formateur.getPrenom());
    stats.put("email", formateur.getEmail());
    
    if (formateur.getDateInscription() != null) {
        stats.put("dateIntegration", formateur.getDateInscription().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    } else {
        stats.put("dateIntegration", "Not availble");
    }
    
    long coursPublies = courseRepository.countByFormateurIdAndStatus(formateurId, CourseStatus.APPROVED);
    long coursEnAttente = courseRepository.countByFormateurIdAndStatus(formateurId, CourseStatus.PENDING);
    long totalCours = courseRepository.countByFormateurId(formateurId);
    
    stats.put("coursPublies", coursPublies);
    stats.put("coursEnAttente", coursEnAttente);
    
    double tauxValidation = totalCours > 0 ? (double) coursPublies / totalCours * 100 : 0;
    stats.put("tauxValidation", Math.round(tauxValidation * 100.0) / 100.0); 
    
    LocalDateTime dernierePublication = courseRepository.findLastPublishedDate(formateurId);
    if (dernierePublication != null) {
        stats.put("dateDernierCours", dernierePublication.toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    } else {
        stats.put("dateDernierCours", "Aucun cours publié");
    }
    
    List<Object[]> categoriesRaw = courseRepository.countByCategorie(formateurId);

    Map<String, Long> categoriesMap = new HashMap<>();
    for (Object[] row : categoriesRaw) {
        categoriesMap.put((String)row[0], (Long)row[1]);
    }
    
    stats.put("coursParCategorie", categoriesMap);    stats.put("coursParLangue", courseRepository.countByLangue(formateurId));
    
    stats.put("etudiantsAbonnes", subscriptionRepository.countApprenantsByFormateurId(formateurId));
    
    
    return stats;
}
public Long getTotalFormateursCount() {
    return formateurRepository.count();
}

public Double getAverageCoursesPerTrainer() {
    long totalCourses = courseRepository.countByStatus(CourseStatus.APPROVED);
    long totalFormateurs = formateurRepository.count();
    return totalFormateurs > 0 ? (double) totalCourses / totalFormateurs : 0;
}

}