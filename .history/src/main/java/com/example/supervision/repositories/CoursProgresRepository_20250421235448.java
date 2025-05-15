package com.example.supervision.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.supervision.classes.CoursProgres;

public interface CoursProgresRepository extends JpaRepository<CoursProgres, Long> {
    /*@Query("SELECT cp.domaine, AVG(cp.completionPourcentage) FROM CoursProgress cp WHERE cp.apprenantId = :id GROUP BY cp.domaine")
    List<Object[]> getAverageProgressByDomain(Long id);

    @Query("SELECT cp.domaine AS domaine, AVG(cp.completionPourcentage) AS moyenne FROM CoursProgress cp WHERE cp.apprenantId = :apprenantId GROUP BY cp.domaine")
    List<Object[]> getAverageProgressByDomain(@Param("apprenantId") Long apprenantId);
}
