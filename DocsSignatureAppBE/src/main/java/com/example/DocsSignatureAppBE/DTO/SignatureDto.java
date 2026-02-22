package com.example.DocsSignatureAppBE.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO for signature information
 */
@Schema(description = "Signature information including coordinates and status")
public class SignatureDto {

    @Schema(description = "Signature ID", example = "1")
    private Long id;

    @Schema(description = "Document ID", example = "5")
    private Long documentId;

    @Schema(description = "Signer username", example = "john_doe")
    private String signerUsername;

    @Schema(description = "X coordinate on PDF", example = "100.5")
    private Float xCoordinate;

    @Schema(description = "Y coordinate on PDF", example = "200.5")
    private Float yCoordinate;

    @Schema(description = "Page number where signature appears", example = "1")
    private Integer pageNumber;

    @Schema(description = "Type of signature", example = "DIGITAL")
    private String signatureType;

    @Schema(description = "Status of signature", example = "PENDING")
    private String status;

    @Schema(description = "Date when signed", example = "2026-02-18T15:30:00")
    private LocalDateTime signedDate;

    @Schema(description = "Reason for signature", example = "Document approval")
    private String reason;

    @Schema(description = "Location of signer", example = "New York, USA")
    private String location;

    @Schema(description = "Signature width in pixels", example = "150")
    private Float width;

    @Schema(description = "Signature height in pixels", example = "50")
    private Float height;

    @Schema(description = "Creation date", example = "2026-02-18T10:00:00")
    private LocalDateTime createdDate;

    @Schema(description = "Last update date", example = "2026-02-18T15:30:00")
    private LocalDateTime updatedDate;

    // Constructors
    public SignatureDto() {}

    public SignatureDto(Long id, Long documentId, String signerUsername, Float xCoordinate,
                       Float yCoordinate, Integer pageNumber, String signatureType, String status) {
        this.id = id;
        this.documentId = documentId;
        this.signerUsername = signerUsername;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.pageNumber = pageNumber;
        this.signatureType = signatureType;
        this.status = status;
    }

    public SignatureDto(Long id, Long documentId, String signerUsername, Float xCoordinate,
                       Float yCoordinate, Integer pageNumber, String signatureType, String status,
                       LocalDateTime signedDate, String reason, String location,
                       Float width, Float height) {
        this(id, documentId, signerUsername, xCoordinate, yCoordinate, pageNumber, signatureType, status);
        this.signedDate = signedDate;
        this.reason = reason;
        this.location = location;
        this.width = width;
        this.height = height;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getSignerUsername() {
        return signerUsername;
    }

    public void setSignerUsername(String signerUsername) {
        this.signerUsername = signerUsername;
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

    public String getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(String signatureType) {
        this.signatureType = signatureType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDateTime signedDate) {
        this.signedDate = signedDate;
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
}

