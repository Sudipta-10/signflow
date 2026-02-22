package com.example.DocsSignatureAppBE.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * Entity representing a signature on a document
 * Stores signature metadata including coordinates, signer info, and status
 */
@Entity
@Table(name = "signatures", indexes = {
    @Index(name = "idx_document_id", columnList = "document_id"),
    @Index(name = "idx_signer_id", columnList = "signer_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signer_id", nullable = false)
    private User signer;

    @Column(name = "x_coordinate", nullable = false)
    private Float xCoordinate;

    @Column(name = "y_coordinate", nullable = false)
    private Float yCoordinate;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "signature_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SignatureType signatureType = SignatureType.DIGITAL;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SignatureStatus status = SignatureStatus.PENDING;

    @Column(name = "signed_date")
    private LocalDateTime signedDate;

    @Column(name = "signature_image_path")
    private String signatureImagePath;

    @Column(name = "signature_data", columnDefinition = "LONGTEXT")
    private String signatureData;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "reason")
    private String reason;

    @Column(name = "location")
    private String location;

    @Column(name = "width")
    private Float width;

    @Column(name = "height")
    private Float height;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @JsonIgnore
    public User getSigner() {
        return signer;
    }

    public void setSigner(User signer) {
        this.signer = signer;
    }

    public Float getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(Float xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public Float getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(Float yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public SignatureType getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(SignatureType signatureType) {
        this.signatureType = signatureType;
    }

    public SignatureStatus getStatus() {
        return status;
    }

    public void setStatus(SignatureStatus status) {
        this.status = status;
    }

    public LocalDateTime getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDateTime signedDate) {
        this.signedDate = signedDate;
    }

    public String getSignatureImagePath() {
        return signatureImagePath;
    }

    public void setSignatureImagePath(String signatureImagePath) {
        this.signatureImagePath = signatureImagePath;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        if (this.createdDate == null) {
            this.createdDate = LocalDateTime.now();
        }
        if (this.updatedDate == null) {
            this.updatedDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = SignatureStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Mark signature as signed
     */
    public void sign(String signatureData) {
        this.status = SignatureStatus.SIGNED;
        this.signedDate = LocalDateTime.now();
        this.signatureData = signatureData;
    }

    /**
     * Mark signature as rejected
     */
    public void reject(String reason) {
        this.status = SignatureStatus.REJECTED;
        this.reason = reason;
    }

    /**
     * Check if signature is pending
     */
    public boolean isPending() {
        return this.status == SignatureStatus.PENDING;
    }

    /**
     * Check if signature is complete
     */
    public boolean isSigned() {
        return this.status == SignatureStatus.SIGNED;
    }
}

