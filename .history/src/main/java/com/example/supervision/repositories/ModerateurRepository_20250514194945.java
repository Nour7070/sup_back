    package com.example.supervision.repositories;

    import com.example.supervision.classes.Apprenant;
    import com.example.supervision.classes.Moderateur;
    import com.example.supervision.classes.User;

    import java.util.List;
    import java.util.Optional;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface ModerateurRepository extends JpaRepository<User, Long> {
        List<User> findByUserType(String userType);

        Optional<Moderateur> findByEmail(String email);

    }
