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


    //Extrait le texte d'un PDF/PPTX
    public String extractContentFromFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        
        if (contentType.equals("application/pdf")) {
            return extractTextFromPdf(file);
        } else if (contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
            return extractTextFromPptx(file);
        }
        throw new IllegalArgumentException("Unsupported file type: " + contentType);
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromPptx(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (InputStream is = file.getInputStream();
             XMLSlideShow ppt = new XMLSlideShow(is)) {
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFTextShape shape : slide.getPlaceholders()) {
                    if (shape != null) {
                        content.append(shape.getText()).append("\n");
                    }
                }
            }
        }
        return content.toString();
    }
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