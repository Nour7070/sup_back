package com.example.supervision.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.supervision.classes.FormateurSubscription;

public interface FormateurSubscriptionRepository extends JpaRepository<FormateurSubscription, Long> {
   
    int countByApprenantId(Long apprenantId);

    int countByFormateurId(Long formateurId);

}