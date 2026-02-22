package com.example.DocsSignatureAppBE.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private String content;

    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @Column(name = "is_signature_required", nullable = false)
    private Boolean isSignatureRequired = false;

    @Column(name = "is_signed", nullable = false)
    private Boolean isSigned = false;

    @Column(name = "file_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileStatus fileStatus = FileStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User uploadedBy;

    @Column(name = "description")
    private String description;

    // New fields for final signed file
    @Column(name = "final_signed_file_path")
    private String finalSignedFilePath;

    @Column(name = "signed_by")
    private String signedBy;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    // Constructors

    public Document(String filename, String filePath, String content, User uploadedBy) {
        this.filename = filename;
        this.originalFilename = filename;
        this.filePath = filePath;
        this.content = content;
        this.uploadedBy = uploadedBy;
       this.uploadDate = LocalDateTime.now();
        this.fileStatus = FileStatus.PENDING;
        this.isSigned = false;
        this.isSignatureRequired = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Boolean getIsSignatureRequired() {
        return isSignatureRequired;
    }

    public void setIsSignatureRequired(Boolean signatureRequired) {
        isSignatureRequired = signatureRequired;
    }

    public Boolean getIsSigned() {
        return isSigned;
    }

    public void setIsSigned(Boolean signed) {
        isSigned = signed;
    }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    @JsonIgnore
    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFinalSignedFilePath() {
        return finalSignedFilePath;
    }

    public void setFinalSignedFilePath(String finalSignedFilePath) {
        this.finalSignedFilePath = finalSignedFilePath;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.uploadDate == null) {
            this.uploadDate = LocalDateTime.now();
        }
        if (this.fileStatus == null) {
            this.fileStatus = FileStatus.PENDING;
        }
        if (this.isSigned == null) {
            this.isSigned = false;
        }
        if (this.isSignatureRequired == null) {
            this.isSignatureRequired = false;
        }
    }
}


