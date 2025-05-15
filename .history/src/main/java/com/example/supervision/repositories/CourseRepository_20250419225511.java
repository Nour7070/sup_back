package com.example.supervision.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Cours, UUID> {
      @Query("SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.chapters WHERE c.status = :status")
    List<Cours> findByStatusWithChapters(@Param("status") CourseStatus status);

    List<Cours> findByFormateurId(Long formateurId);
    List<Cours> findByFormateurIdAndStatus(Long formateurId, CourseStatus status);

    @Query("SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.chapters WHERE c.formateurId = :formateurId AND (:status IS NULL OR c.status = :status)")
    List<Cours> findByFormateurWithChapters(@Param("formateurId") Long formateurId, 
                                          @Param("status") CourseStatus status);

    // Méthode pour trouver tous les cours par IDs (utilisée pour la suppression multiple)
    List<Cours> findAllByIdIn(List<UUID> ids);
    
    // Méthode pour vérifier l'existence d'un cours par ID
    boolean existsById(UUID id);
    
    // Méthode personnalisée pour supprimer par ID avec retour booléen
    @Modifying
    @Query("DELETE FROM Cours c WHERE c.id = :id")
    @Transactional
    int deleteByIdAndReturnCount(@Param("id") UUID id);

    Optional<Cours> findById(UUID id);

}