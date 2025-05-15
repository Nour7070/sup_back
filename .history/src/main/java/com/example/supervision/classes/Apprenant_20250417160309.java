package com.example.supervision.classes;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("APPRENANT")
public class Apprenant extends User {
    

}