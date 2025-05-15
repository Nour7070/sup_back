package com.example.supervision.classes;

@Entity
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long apprenantId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}