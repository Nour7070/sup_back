package com.example.supervision.classes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.ToString;

@Entity
@Table(name = "courses")
public class Cours {
    /*@Id
    @GeneratedValue(generator = "UUID") 
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String titre;
    private Long formateurId;
    private Long moderateurId;
    private String description;
    private String domaine;

    @Enumerated(EnumType.ORDINAL)
    private Langue langue;

    private Long approvedBy; 

    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.PENDING;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("cours")
    @ToString.Exclude
    private List<Chapter> chapters = new ArrayList<>();


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /*@ManyToMany
    @JoinTable(
        name = "apprenant_cours",
        joinColumns = @JoinColumn(name = "cours_id"),
        inverseJoinColumns = @JoinColumn(name = "apprenant_id")
    )
    private Set<Apprenant> apprenants = new HashSet<>();*/

    public Cours() {
    }

    public Cours(UUID id, String titre, Long formateurId) {
        this.id = id;
        this.titre = titre;
        this.formateurId = formateurId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Long getFormateurId() {
        return formateurId;
    }

    public void setFormateurId(Long formateurId) {
        this.formateurId = formateurId;
    }

    public Long getModerateurId() {
        return moderateurId;
    }

    public void setModerateurId(Long moderateurId) {
        this.moderateurId = moderateurId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Langue getLangue() {
        return langue;
    }

    public void setLangue(Langue langue) {
        this.langue = langue;
    }

    public String getDomaine() {
        return domaine;
    }

    public void setDomaine(String domaine) {
        this.domaine = domaine;
    }

    /*public void setDomaine(String domaine) {
        this.domaine = domaine != null ? domaine.trim().toLowerCase() : "Non spécifié";
    }*/
    
    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
  
    public Long getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

   
    /*public Set<Apprenant> getApprenants() {
        return apprenants;
    }
    
    public void setApprenants(Set<Apprenant> apprenants) {
        this.apprenants = apprenants;
    }

    public void addApprenant(Apprenant apprenant) {
        this.apprenants.add(apprenant);
        Set<Cours> coursSet = new HashSet<>();
        if (apprenant.getCoursSuivis() instanceof Set) {
            coursSet = (Set<Cours>) apprenant.getCoursSuivis();
        }
        coursSet.add(this);
        apprenant.setCoursSuivis(coursSet);
    }
    
    public void removeApprenant(Apprenant apprenant) {
        this.apprenants.remove(apprenant);
        if (apprenant.getCoursSuivis() instanceof Set) {
            Set<Cours> coursSet = (Set<Cours>) apprenant.getCoursSuivis();
            coursSet.remove(this);
            apprenant.setCoursSuivis(coursSet);
        }
    }*/
}
