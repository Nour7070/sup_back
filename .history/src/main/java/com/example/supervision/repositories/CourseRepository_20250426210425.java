package com.example.supervision.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Cours, UUID> {
      @Query("SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.chapters WHERE c.status = :status")
    List<Cours> findByStatusWithChapters(@Param("status") CourseStatus status);

    List<Cours> findByFormateurId(Long formateurId);
    List<Cours> findByFormateurIdAndStatus(Long formateurId, CourseStatus status);

    @Query("SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.chapters WHERE c.formateurId = :formateurId AND (:status IS NULL OR c.status = :status)")
    List<Cours> findByFormateurWithChapters(@Param("formateurId") Long formateurId, 
                                          @Param("status") CourseStatus status);
    
    long countByFormateurIdAndStatus(Long formateurId, CourseStatus status);

    @Query("SELECT c.domaine, MONTH(c.createdAt), COUNT(c) " +
    "FROM Cours c WHERE YEAR(c.createdAt) = :year " +
    "GROUP BY c.domaine, MONTH(c.createdAt)")
    List<Object[]> findCoursesCreatedPerDomainByMonth(@Param("year") int year);

    long countByFormateurId(Long formateurId);

    @Query("SELECT MAX(c.createdAt) FROM Cours c WHERE c.formateurId = :formateurId AND c.status = 'APPROVED'")
    LocalDateTime findLastPublishedDate(Long formateurId);

    @Query("SELECT c.domaine, COUNT(c) FROM Cours c WHERE c.formateurId = :formateurId GROUP BY c.domaine")
    List<Object[]> countByCategorie(Long formateurId);

    
    @Query("SELECT c.langue, COUNT(c) FROM Cours c WHERE c.formateurId = :formateurId GROUP BY c.langue")
    List<Object[]> countByLangue(Long formateurId);

     // Compte les cours par statut
     long countByStatus(CourseStatus status);
     
     // Compte les cours approuvés par formateur
     @Query("SELECT COUNT(c) FROM Cours c WHERE c.formateurId = :formateurId AND c.status = 'APPROVED'")
     long countApprovedByFormateurId(Long formateurId);
     
     // Compte les cours en attente par formateur
     @Query("SELECT COUNT(c) FROM Cours c WHERE c.formateurId = :formateurId AND c.status = 'PENDING'")
     long countPendingByFormateurId(Long formateurId);

     @Query("SELECT c.domaine, COUNT(c), COUNT(CASE WHEN c.status = 'APPROVED' THEN 1 END) " +
       "FROM Cours c GROUP BY c.domaine")
        List<Object[]> findCoursesCountByDomain();

      @Query("SELECT c.id, c.titre, c.domaine, COUNT(e), SIZE(c.chapters) " +
       "FROM Cours c JOIN c.etudiants e WHERE c.status = 'APPROVED' " +
       "GROUP BY c.id, c.titre, c.domaine")
        List<Object[]> findCoursesWithStudentCount();
    
}