package com.example.supervision.classes;

@Entity
public class FormateurSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long formateurId;
    private Long apprenantId;
    private LocalDateTime subscribedAt;
}