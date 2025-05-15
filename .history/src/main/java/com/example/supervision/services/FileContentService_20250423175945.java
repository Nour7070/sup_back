package com.example.supervision.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class FileContentService {

    @Value("${file.upload-dir}")
    private String uploadDir;

     public String extractContentFromStoredFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        try (InputStream is = Files.newInputStream(filePath)) {
            if (filename.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(is)) {
                    return new PDFTextStripper().getText(document);
                }
            } else if (filename.endsWith(".pptx")) {
                // ... implémentation similaire pour PPTX ...
            }
            throw new IllegalArgumentException("Type de fichier non supporté");
        }
    }
    
}