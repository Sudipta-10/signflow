package com.example.DocsSignatureAppBE.DTO;

import java.time.LocalDateTime;

public class DocumentDto {
    private Long id;
    private String filename;
    private String originalFilename;
    private String content;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadDate;
    private Boolean isSigned;
    private String fileStatus;

    public DocumentDto() {}

    public DocumentDto(Long id, String filename, String originalFilename, Long fileSize, String mimeType, LocalDateTime uploadDate, Boolean isSigned, String fileStatus) {
        this.id = id;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.uploadDate = uploadDate;
        this.isSigned = isSigned;
        this.fileStatus = fileStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public Boolean getIsSigned() { return isSigned; }
    public void setIsSigned(Boolean isSigned) { this.isSigned = isSigned; }

    public String getFileStatus() { return fileStatus; }
    public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }
}
