package com.example.supervision.classes;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
@DiscriminatorValue("FORMATEUR")
public class Formateur extends User {
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Date dateInscription;

    private long coursPublies;

    private long coursEnAttente;

    private long etudiantsAbonnes;

    @ManyToMany
    @JoinTable(
        name = "formateur_apprenant",
        joinColumns = @JoinColumn(name = "formateur_id"),
        inverseJoinColumns = @JoinColumn(name = "apprenant_id")
    )
    @JsonIgnore // Important pour éviter les cycles dans le JSON
    private Set<User> apprenantsAbonnes = new HashSet<>();
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
    
    public long getCoursPublies() 
    { 
        return coursPublies; 
    }

    public void setCoursPublies(long coursPublies) 
    { 
        this.coursPublies = coursPublies; 
    }
    
    public long getCoursEnAttente() 
    { 
        return coursEnAttente; 
    }

    public void setCoursEnAttente(long coursEnAttente) 
    { 
        this.coursEnAttente = coursEnAttente; 
    }
    
    public long getEtudiantsAbonnes() 
    { 
        return etudiantsAbonnes; 
    }

    public void setEtudiantsAbonnes(long etudiantsAbonnes) 
    { 
        this.etudiantsAbonnes = etudiantsAbonnes; 
    }
}