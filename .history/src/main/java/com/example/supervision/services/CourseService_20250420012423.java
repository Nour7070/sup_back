package com.example.supervision.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service

public class CourseService {

    private final CourseRepository repository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileService;

    public CourseService (CourseRepository repository ,
    ChapterRepository chapterRepository ,
    SupervisionFileService fileService ){
        this.repository=repository;
        this.chapterRepository= chapterRepository;
        this.fileService= fileService ;
    }

    public List<Cours> getAllPendingCourses() {
        return repository.findByStatusWithChapters(CourseStatus.PENDING);
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
    /*public Cours simulCours(Cours cours) {

        cours.setStatus(CourseStatus.PENDING);
        return repository.save(cours); 
    }*/

     @Transactional
    public Cours createCourseWithChapters(Cours courseRequest, List<MultipartFile> files) throws IOException {
        // 1. Validation et préparation des chapitres
        List<Chapter> chapters = processUploadedFiles(files);
        
        // 2. Configuration du cours
        courseRequest.setStatus(CourseStatus.PENDING);
        courseRequest.setChapters(chapters);
        
        // 3. Sauvegarde
        Cours savedCourse = repository.save(courseRequest);
        saveChaptersWithCourseReference(chapters, savedCourse);
        
        return savedCourse;
    }

    private List<Chapter> processUploadedFiles(List<MultipartFile> files) throws IOException {
        List<Chapter> chapters = new ArrayList<>();
        
        for (MultipartFile file : files) {
            validateFileType(file);
            String fileUrl = storeFileAndGetUrl(file);
            chapters.add(createChapterFromFile(file, fileUrl));
        }
        
        return chapters;
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (!isSupportedFileType(contentType)) {
            throw new IllegalArgumentException("Format de fichier non supporté: " + contentType);
        }
    }

    private boolean isSupportedFileType(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") || 
            contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
        );
    }

    private String storeFileAndGetUrl(MultipartFile file) throws IOException {
        return fileService.storeFile(file);
    }

    private Chapter createChapterFromFile(MultipartFile file, String fileUrl) {
        Chapter chapter = new Chapter();
        chapter.setTitle(extractFileNameWithoutExtension(file.getOriginalFilename()));
        chapter.setFileUrl(fileUrl);
        return chapter;
    }

    private String extractFileNameWithoutExtension(String filename) {
        return filename != null ? filename.replaceFirst("[.][^.]+$", "") : "Sans titre";
    }

    private void saveChaptersWithCourseReference(List<Chapter> chapters, Cours cours) {
        chapters.forEach(chapter -> {
            chapter.setCours(cours);
            chapterRepository.save(chapter);
        });
    }
    @Transactional(readOnly = true)
public List<Cours> getCoursesByFormateur(Long formateurId, CourseStatus status) {
    return repository.findByFormateurWithChapters(formateurId, status);
}

public List<Cours> countCoursesByFormateurAndStatus(Long formateurId, CourseStatus status) {
    return repository.findByFormateurIdAndStatus(formateurId, status);

    
}
}