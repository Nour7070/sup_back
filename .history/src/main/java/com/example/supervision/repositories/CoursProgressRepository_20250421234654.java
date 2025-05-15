package com.example.supervision.repositories;

public interface CoursProgressRepository extends JpaRepository<CoursProgress, Long> {
    @Query("SELECT cp.domaine, AVG(cp.completionPourcentage) FROM CoursProgress cp WHERE cp.apprenantId = :id GROUP BY cp.domaine")
    List<Object[]> getAverageProgressByDomain(Long id);
}
