package com.example.supervision.classes;

import java.util.Date;
import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("FORMATEUR")
public class Formateur extends User {
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Date dateInscription;

    private List<Cours> coursPublies;

    private long coursEnAttente;

    private long etudiantsAbonnes;

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
    
    public List<Cours> getCoursPublies() 
    { 
        return coursPublies; 
    }

    public void setCoursPublies(List<Cours> coursPublies) 
    { 
        this.coursPublies = coursPublies; 
    }
    
    public long getCoursEnAttente() 
    { 
        return coursEnAttente; 
    }

    public void setCoursEnAttente(List<Cours> coursEnAttente) 
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