package com.example.supervision.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.supervision.classes.FormateurSubscription;

public interface FormateurSubscriptionRepository extends JpaRepository<FormateurSubscription, Long> {
   
    int countByApprenantId(Long apprenantId);

    int countApprenantsByFormateurId(Long formateurId);

}