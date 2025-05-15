package com.example.supervision.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;


@Service

public class CourseService {

    private final CourseRepository repository;


    public CourseService(CourseRepository repository) {
    this.repository = repository;
}
    public boolean validateCourse(UUID id, boolean approve) {
        Optional<Cours> optionalCours = repository.findById(id);
        if (optionalCours.isEmpty()) {
            return false;
        }

        Cours cours = optionalCours.get();
        cours.setStatus(approve ? CourseStatus.APPROVED : CourseStatus.REJECTED);
        repository.save(cours);
        return true;
    }


    //Avoir les cours par formateurs ala hssab le statut 
    @Transactional(readOnly = true)
    public List<Cours> getCoursesByFormateur(Long formateurId, CourseStatus status) {
      return repository.findByFormateurWithChapters(formateurId, status);
    }


    public List<Object[]> getCoursesCreatedPerDomainByMonth(int year) {
      return repository.findCoursesCreatedPerDomainByMonth(year);
    }

}