package com.example.supervision.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.userType = :userType")
    List<User> findByUserType(@Param("userType") String userType);

    Optional<User> findById(long formateurId);

    @Query(value = "SELECT * FROM users WHERE user_type = 'FORMATEUR' AND status = 'PENDING'", nativeQuery = true)
    List<User> findPendingFormateurs();

    long countByUserType(String string);

    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(a) FROM Apprenant a JOIN a.formateursSuivis f WHERE f.id = :formateurId")
    long countApprenantsByFormateurId(@Param("formateurId") Long formateurId);

   
}
