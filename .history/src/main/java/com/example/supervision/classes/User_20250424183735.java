package com.example.supervision.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.DiscriminatorType;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "id"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "nom")
    private String nom;

    @Column(name = "username")
    private String username;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "user_type", insertable = false, updatable = false)
    private String userType;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @JsonIgnore
    @Version
    private Integer version = 0;

    @JsonIgnore
    @Column(name = "adresse")
    private String address;

    @JsonIgnore
    @Column(name = "photo" , nullable=true )
    private String photo;

    @JsonIgnore
    @Column(name = "Num")
    private String phoneNumber;

    public User() {
        this.version = 0;
    }

    public User(Long id) {
        this.id = id;
        this.version = 0;
    }

    public User(Long id, String userType) {
        this.id = id;
        this.userType = userType;
        this.version = 0;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNom() {
        return nom;
    }

    public String getUsername() {
        return username;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getUserType() {
        return userType;
    }

    public String getPassword() {
        return password;
    }

    public Integer getVersion() {
        return version;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoto() {
        return photo;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @PrePersist
    protected void onCreate() {
        if (version == null) {
            version = 0;
        }
    }
}