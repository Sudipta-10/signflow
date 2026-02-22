package com.example.DocsSignatureAppBE.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO for submitting signature via drag-and-drop UI
 * Captures coordinates from relative mouse position on PDF viewer
 */
@Schema(description = "Signature submission with drag-and-drop coordinates")
public class SignaturePlacementRequest {

    @NotNull(message = "Signature ID is required")
    @Schema(description = "Signature placeholder ID", example = "1")
    private Long signatureId;

    @NotNull(message = "X coordinate is required")
    @Min(0)
    @Max(100)
    @Schema(description = "X coordinate (0-100% of page width)", example = "25.5")
    private Float xCoordinate;

    @NotNull(message = "Y coordinate is required")
    @Min(0)
    @Max(100)
    @Schema(description = "Y coordinate (0-100% of page height)", example = "75.3")
    private Float yCoordinate;

    @Min(1)
    @Schema(description = "Page number (1-based)", example = "1")
    private Integer pageNumber;

    @NotNull(message = "Signature data is required")
    @Schema(description = "Signature image data (base64 or SVG path)", example = "data:image/png;base64,...")
    private String signatureData;

    @Schema(description = "Signature width in pixels", example = "150")
    private Float width;

    @Schema(description = "Signature height in pixels", example = "50")
    private Float height;

    @Schema(description = "Reason for signing", example = "Document approved")
    private String reason;

    @Schema(description = "Location of signer", example = "New York, USA")
    private String location;

    @Schema(description = "Signature type", example = "DIGITAL")
    private String signatureType;

    @Schema(description = "Optional notes", example = "Approved by manager")
    private String notes;

    // Constructors
    public SignaturePlacementRequest() {}

    public SignaturePlacementRequest(Long signatureId, Float xCoordinate, Float yCoordinate,
                                    Integer pageNumber, String signatureData) {
        this.signatureId = signatureId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.pageNumber = pageNumber;
        this.signatureData = signatureData;
    }

    // Getters and Setters
    public Long getSignatureId() {
        return signatureId;
    }

    public void setSignatureId(Long signatureId) {
        this.signatureId = signatureId;
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

    public String getSignatureData() {
        return signatureData;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
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

    public String getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(String signatureType) {
        this.signatureType = signatureType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

