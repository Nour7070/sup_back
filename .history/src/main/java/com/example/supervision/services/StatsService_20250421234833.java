package com.example.supervision.services;

@Service
public class StatsService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private FormateurSubscriptionRepository subscriptionRepo;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private CoursProgressRepository coursProgressRepository;

    public Map<String, Object> getFormateurStats(Long formateurId) {
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
    }

    public Map<String, Object> getApprenantStats(Long apprenantId) {
        Map<String, Object> stats = new HashMap<>();

        Long tempsTotalSecondes = sessionRepository.getTotalTimeSpent(apprenantId);
        stats.put("tempsTotalEnSecondes", tempsTotalSecondes != null ? tempsTotalSecondes : 0);

        stats.put("progressionParDomaine", coursProgressRepository.getAverageProgressByDomain(apprenantId));
        stats.put("formateursSuivis", subscriptionRepo.countByApprenantId(apprenantId));

        return stats;
    }
}