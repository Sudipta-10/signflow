package com.example.DocsSignatureAppBE.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO for detailed document information (preview and details page)
 */
@Schema(description = "Detailed document information with all metadata")
public class DocumentDetailDto {

    @Schema(description = "Document ID", example = "1")
    private Long id;

    @Schema(description = "Document title", example = "Sales Contract")
    private String filename;

    @Schema(description = "Original uploaded filename", example = "contract.pdf")
    private String originalFilename;

    @Schema(description = "File path on server", example = "/uploads/doc_1708349200000.pdf")
    private String filePath;

    @Schema(description = "File size in bytes", example = "1024000")
    private Long fileSize;

    @Schema(description = "MIME type", example = "application/pdf")
    private String mimeType;

    @Schema(description = "Document description", example = "Important sales contract")
    private String description;

    @Schema(description = "Upload timestamp", example = "2026-02-18T10:30:00")
    private LocalDateTime uploadDate;

    @Schema(description = "Whether document is signed", example = "false")
    private Boolean isSigned;

    @Schema(description = "Whether signature is required", example = "true")
    private Boolean isSignatureRequired;

    @Schema(description = "Document status", example = "PENDING")
    private String fileStatus;

    @Schema(description = "Username of uploader", example = "john_doe")
    private String uploadedByUsername;

    @Schema(description = "Path to the final signed document", example = "/signed-uploads/signed_contract.pdf")
    private String finalSignedFilePath;

    // Constructors
    public DocumentDetailDto() {}

    public DocumentDetailDto(Long id, String filename, String originalFilename, String filePath,
                           Long fileSize, String mimeType, String description, LocalDateTime uploadDate,
                           Boolean isSigned, Boolean isSignatureRequired, String fileStatus,
                           String uploadedByUsername, String finalSignedFilePath) {
        this.id = id;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.description = description;
        this.uploadDate = uploadDate;
        this.isSigned = isSigned;
        this.isSignatureRequired = isSignatureRequired;
        this.fileStatus = fileStatus;
        this.uploadedByUsername = uploadedByUsername;
        this.finalSignedFilePath = finalSignedFilePath;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Boolean getIsSigned() {
        return isSigned;
    }

    public void setIsSigned(Boolean signed) {
        isSigned = signed;
    }

    public Boolean getIsSignatureRequired() {
        return isSignatureRequired;
    }

    public void setIsSignatureRequired(Boolean signatureRequired) {
        isSignatureRequired = signatureRequired;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getUploadedByUsername() {
        return uploadedByUsername;
    }

    public void setUploadedByUsername(String uploadedByUsername) {
        this.uploadedByUsername = uploadedByUsername;
    }

    public String getFinalSignedFilePath() {
        return finalSignedFilePath;
    }

    public void setFinalSignedFilePath(String finalSignedFilePath) {
        this.finalSignedFilePath = finalSignedFilePath;
    }
}

