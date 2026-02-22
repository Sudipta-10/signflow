package com.example.DocsSignatureAppBE.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * Entity class for tracking file uploads
 * Stores metadata about uploaded files and their processing status
 */
@Entity
@Table(name = "file_uploads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_id", nullable = false, unique = true)
    private String uploadId;

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

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @Column(name = "checksum")
    private String checksum;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileStatus status = FileStatus.PENDING;

    @Column(name = "upload_timestamp", nullable = false, updatable = false)
    private LocalDateTime uploadTimestamp;

    @Column(name = "last_modified_timestamp")
    private LocalDateTime lastModifiedTimestamp;

    @Column(name = "is_virus_scanned", nullable = false)
    private Boolean isVirusScanned = false;

    @Column(name = "virus_scan_status")
    private String virusScanStatus;

    @Column(name = "is_malicious", nullable = false)
    private Boolean isMalicious = false;

    @Column(name = "upload_progress", nullable = false)
    private Integer uploadProgress = 0;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "download_count", nullable = false)
    private Long downloadCount = 0L;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "tags")
    private String tags;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User uploadedBy;

    // Constructors
    public FileUpload(String uploadId, String filename, String originalFilename,
                     String filePath, Long fileSize, String mimeType, String fileExtension, User uploadedBy) {
        this.uploadId = uploadId;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.uploadedBy = uploadedBy;
        this.uploadTimestamp = LocalDateTime.now();
        this.lastModifiedTimestamp = LocalDateTime.now();
        this.status = FileStatus.PENDING;
        this.uploadProgress = 0;
    }

    public FileUpload(String uploadId, String filename, String originalFilename,
                     String filePath, Long fileSize, String mimeType, String fileExtension,
                     String checksum, User uploadedBy) {
        this(uploadId, filename, originalFilename, filePath, fileSize, mimeType, fileExtension, uploadedBy);
        this.checksum = checksum;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
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

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    public LocalDateTime getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(LocalDateTime uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public LocalDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public Boolean getIsVirusScanned() {
        return isVirusScanned;
    }

    public void setIsVirusScanned(Boolean virusScanned) {
        isVirusScanned = virusScanned;
    }

    public String getVirusScanStatus() {
        return virusScanStatus;
    }

    public void setVirusScanStatus(String virusScanStatus) {
        this.virusScanStatus = virusScanStatus;
    }

    public Boolean getIsMalicious() {
        return isMalicious;
    }

    public void setIsMalicious(Boolean malicious) {
        isMalicious = malicious;
    }

    public Integer getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(Integer uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        if (this.uploadTimestamp == null) {
            this.uploadTimestamp = LocalDateTime.now();
        }
        if (this.lastModifiedTimestamp == null) {
            this.lastModifiedTimestamp = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = FileStatus.PENDING;
        }
        if (this.isVirusScanned == null) {
            this.isVirusScanned = false;
        }
        if (this.isMalicious == null) {
            this.isMalicious = false;
        }
        if (this.uploadProgress == null) {
            this.uploadProgress = 0;
        }
        if (this.downloadCount == null) {
            this.downloadCount = 0L;
        }
        if (this.isPublic == null) {
            this.isPublic = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedTimestamp = LocalDateTime.now();
    }

    /**
     * Check if file is ready for download
     * @return true if file is signed and not malicious
     */
    public boolean isReadyForDownload() {
        return !isMalicious && (status == FileStatus.SIGNED || status == FileStatus.PROCESSING);
    }

    /**
     * Check if upload is complete
     * @return true if upload progress is 100%
     */
    public boolean isUploadComplete() {
        return uploadProgress == 100;
    }

    /**
     * Increment download count
     */
    public void incrementDownloadCount() {
        this.downloadCount = this.downloadCount == null ? 1L : this.downloadCount + 1L;
    }
}

