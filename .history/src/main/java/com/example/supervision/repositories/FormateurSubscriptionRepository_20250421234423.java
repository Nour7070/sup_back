package com.example.supervision.repositories;

@Query("SELECT FUNCTION('DATE', fs.subscribedAt), COUNT(fs.id) FROM FormateurSubscription fs WHERE fs.formateurId = :formateurId GROUP BY FUNCTION('DATE', fs.subscribedAt)")
List<Object[]> countSubscriptionsPerDay(Long formateurId);

int countByApprenantId(Long apprenantId);

int countByFormateurId(Long formateurId);