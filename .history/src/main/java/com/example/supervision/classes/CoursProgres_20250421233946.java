package com.example.supervision.classes;

@Entity
public class CoursProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long apprenantId;
    private String domaine;
    private Double completionPourcentage;
}