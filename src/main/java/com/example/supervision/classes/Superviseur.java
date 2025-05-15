package com.example.supervision.classes;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SUPERVISEUR")
public class Superviseur extends User {
    
 
}
