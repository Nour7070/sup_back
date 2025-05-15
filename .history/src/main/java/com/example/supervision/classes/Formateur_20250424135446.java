package com.example.supervision.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

@Entity
@DiscriminatorValue("FORMATEUR")
public class Formateur extends User {
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Date dateInscription;

    private Long coursPublies;

    private Long coursEnAttente;

    private Long etudiantsAbonnes;

    @ManyToMany
    @JoinTable(
        name = "formateur_apprenant",
        joinColumns = @JoinColumn(name = "formateur_id"),
        inverseJoinColumns = @JoinColumn(name = "apprenant_id")
    )
 @JsonIgnore 
    private Set<User> apprenantsAbonnes = new HashSet<>();

    @OneToMany(mappedBy = "formateur", cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    private List<Document> documents = new ArrayList<>();
    
   
    public Set<User> getApprenantsAbonnes() {
        return Collections.unmodifiableSet(apprenantsAbonnes);
    }

    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getDateInscription() 
    { 
        return dateInscription; 
    }

    public void setDateInscription(Date dateInscription) 
    { 
        this.dateInscription = dateInscription; 
    }
    
    public Long getCoursPublies() 
    { 
        return coursPublies; 
    }

    public void setCoursPublies(Long coursPublies) 
    { 
        this.coursPublies = coursPublies; 
    }
    
    public Long getCoursEnAttente() 
    { 
        return coursEnAttente; 
    }

    public void setCoursEnAttente(Long coursEnAttente) 
    { 
        this.coursEnAttente = coursEnAttente; 
    }
    
    public Long getEtudiantsAbonnes() 
    { 
        return etudiantsAbonnes; 
    }

    public void setEtudiantsAbonnes(Long etudiantsAbonnes) 
    { 
        this.etudiantsAbonnes = etudiantsAbonnes; 
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents ;
    }

    public List<Document> getDocuments() {
        return documents;
    }
}