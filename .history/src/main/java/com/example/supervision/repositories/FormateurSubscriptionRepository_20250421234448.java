package com.example.supervision.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.supervision.classes.FormateurSubscription;

public interface FormateurSubscriptionRepository extends JpaRepository<FormateurSubscription, Long> {
    @Query("SELECT FUNCTION('DATE', fs.subscribedAt), COUNT(fs.id) FROM FormateurSubscription fs WHERE fs.formateurId = :formateurId GROUP BY FUNCTION('DATE', fs.subscribedAt)")
    List<Object[]> countSubscriptionsPerDay(Long formateurId);

    int countByApprenantId(Long apprenantId);

    int countByFormateurId(Long formateurId);
}