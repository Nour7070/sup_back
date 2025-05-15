package com.example.supervision.classes;

@Entity
public class CoursProgres {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long apprenantId;
    private String domaine;
    private Double completionPourcentage;
}