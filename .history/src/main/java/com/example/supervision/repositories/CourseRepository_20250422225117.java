package com.example.supervision.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;

import jakarta.transaction.Transactional;

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

    List<Cours> findAllByIdIn(List<UUID> ids);
    
    @Modifying
    @Query("DELETE FROM Cours c WHERE c.id = :id")
    @Transactional
    int deleteByIdAndReturnCount(@Param("id") UUID id);

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

    
  
}