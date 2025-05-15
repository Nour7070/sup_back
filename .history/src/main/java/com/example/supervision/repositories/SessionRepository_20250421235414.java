package com.example.supervision.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import com.example.supervision.classes.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {
    @Query("SELECT SUM(TIMESTAMPDIFF(SECOND, s.startTime, s.endTime)) FROM Session s WHERE s.apprenantId = :id")
    Long getTotalTimeSpent(Long id);

}