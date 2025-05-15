package com.example.supervision.repositories;

public interface SessionRepository extends JpaRepository<Session, Long> {
    @Query("SELECT SUM(TIMESTAMPDIFF(SECOND, s.startTime, s.endTime)) FROM Session s WHERE s.apprenantId = :id")
    Long getTotalTimeSpent(Long id);
}