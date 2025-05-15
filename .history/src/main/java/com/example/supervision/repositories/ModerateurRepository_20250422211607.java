package com.example.supervision.repositories;

import com.example.supervision.classes.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerateurRepository extends JpaRepository<User, Long> {
    List<User> findByUserType(String userType);

     @Query("SELECT COUNT(m) FROM Moderateur m")
     long countTotalModerateurs();
     
     @Query("SELECT SUM(m.actionsCount) FROM Moderateur m")
     Long countTotalActions();
     
     @Query("SELECT m.id, m.actionsCount FROM Moderateur m")
     List<Object[]> findModerateurActions();

}
