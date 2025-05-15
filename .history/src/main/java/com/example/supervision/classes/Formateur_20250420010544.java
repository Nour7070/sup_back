package com.example.supervision.classes;

import java.util.Date;

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

    private int coursPublies;

    private int coursEnAttente;

    private int etudiantsAbonnes;

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
    
    public int getCoursPublies() 
    { 
        return coursPublies; 
    }

    public void setCoursPublies(int coursPublies) 
    { 
        this.coursPublies = coursPublies; 
    }
    
    public int getCoursEnAttente() 
    { 
        return coursEnAttente; 
    }

    public void setCoursEnAttente(int coursEnAttente) 
    { 
        this.coursEnAttente = coursEnAttente; 
    }
    
    public int getEtudiantsAbonnes() 
    { 
        return etudiantsAbonnes; 
    }

    public void setEtudiantsAbonnes(int etudiantsAbonnes) 
    { 
        this.etudiantsAbonnes = etudiantsAbonnes; 
    }
}