package com.example.supervision.classes;

public class FileContent {
    private String fileName;
    private String content;
    private String fileType;

    public FileContent(String fileName, String content, String fileType) {
        this.fileName = fileName;
        this.content = content;
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "FileContent{" +
               "fileName='" + fileName + '\'' +
               ", content='" + content + '\'' +
               ", fileType='" + fileType + '\'' +
               '}';
    }
}