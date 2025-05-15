package com.example.supervision.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;


@Service

public class CourseService {

    private final CourseRepository repository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileService;


    public CourseService(CourseRepository repository,
                   ChapterRepository chapterRepository,
                   SupervisionFileService fileService) {
    this.repository = repository;
    this.chapterRepository = chapterRepository;
    this.fileService = fileService ;
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