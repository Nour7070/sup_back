package com.example.supervision.services;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Formateur;
import com.example.supervision.repositories.CoursProgresRepository;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.repositories.FormateurSubscriptionRepository;
import com.example.supervision.repositories.SessionRepository;

@Service
public class StatsService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private FormateurSubscriptionRepository subscriptionRepo;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private CoursProgresRepository coursProgresRepository;
    private FormateurRepository formateurRepository ;

    /*public Map<String, Object> getFormateurStats(Long formateurId) {
        Map<String, Object> stats = new HashMap<>();

        long total = courseRepository.countByFormateurId(formateurId);
        long approuves = courseRepository.countByFormateurIdAndStatus(formateurId, CourseStatus.APPROVED);
        long enAttente = courseRepository.countByFormateurIdAndStatus(formateurId, CourseStatus.PENDING);

        double tauxValidation = total > 0 ? (double) approuves / total * 100 : 0;

        stats.put("coursPublies", approuves);
        stats.put("coursEnAttente", enAttente);
        stats.put("tauxValidation", tauxValidation);
        stats.put("dateDernierCours", courseRepository.findLastPublishedDate(formateurId));

        stats.put("subscriptionsParJour", subscriptionRepo.countSubscriptionsPerDay(formateurId));
        stats.put("coursParCategorie", courseRepository.countByCategorie(formateurId));
        stats.put("coursParLangue", courseRepository.countByLangue(formateurId));
        stats.put("etudiantsAbonnes", subscriptionRepo.countByFormateurId(formateurId));

        return stats;
    }*/

    public Map<String, Object> getFormateurStats(Long formateurId) {
    Map<String, Object> stats = new HashMap<>();
    
    // Données existantes
    long total = courseRepository.countByFormateurId(formateurId);
    long approuves = courseRepository.countByFormateurIdAndStatus(formateurId, CourseStatus.APPROVED);
    long enAttente = courseRepository.countByFormateurIdAndStatus(formateurId, CourseStatus.PENDING);
    
    // Trouver les informations de base du formateur
    Formateur formateur = formateurRepository.findById(formateurId)
        .orElseThrow(() -> new ResourceNotFoundException("Formateur not found"));
    
    // Ajouter les infos de base
    stats.put("name", formateur.getNom() + " " + formateur.getPrenom());
    stats.put("email", formateur.getEmail());
    
    // Ajouter les statistiques des cours
    stats.put("coursPublies", approuves);
    stats.put("coursEnAttente", enAttente);
    stats.put("tauxValidation", tauxValidation);
    
    // Ajouter le nombre de quizz
    stats.put("quizzPublies", quizzRepository.countByFormateurId(formateurId));
    
    // Reste du code existant
    stats.put("etudiantsAbonnes", subscriptionRepo.countByFormateurId(formateurId));
    
    return stats;
}

public Map<String, Object> getApprenantStats(Long apprenantId) {
    Map<String, Object> stats = new HashMap<>();
    
    // Trouver les informations de base de l'apprenant
    Apprenant apprenant = apprenantRepository.findById(apprenantId)
        .orElseThrow(() -> new ResourceNotFoundException("Apprenant not found"));
    
    // Ajouter les infos de base
    stats.put("name", apprenant.getNom() + " " + apprenant.getPrenom());
    stats.put("email", apprenant.getEmail());
    stats.put("dateIntegration", apprenant.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    
    // Statistiques des cours
    stats.put("coursSuivis", inscriptionRepository.countByApprenantId(apprenantId));
    
    // Statistiques des quizz
    stats.put("quizzReussis", quizzResultRepository.countPassedQuizzesByApprenantId(apprenantId));
    stats.put("quizzEchoues", quizzResultRepository.countFailedQuizzesByApprenantId(apprenantId));
    
    // Reste du code existant
    stats.put("tempsTotalEnSecondes", tempsTotalSecondes != null ? tempsTotalSecondes : 0);
    
    return stats;
}

// Méthode utilitaire pour calculer l'âge
private int calculateAge(LocalDate dateNaissance) {
    return Period.between(dateNaissance, LocalDate.now()).getYears();
}

    public Map<String, Object> getApprenantStats(Long apprenantId) {
        Map<String, Object> stats = new HashMap<>();

        Long tempsTotalSecondes = sessionRepository.getTotalTimeSpent(apprenantId);
        stats.put("tempsTotalEnSecondes", tempsTotalSecondes != null ? tempsTotalSecondes : 0);

        stats.put("progressionParDomaine", coursProgresRepository.getAverageProgressByDomain(apprenantId));
        stats.put("formateursSuivis", subscriptionRepo.countByApprenantId(apprenantId));

        return stats;
    }
}