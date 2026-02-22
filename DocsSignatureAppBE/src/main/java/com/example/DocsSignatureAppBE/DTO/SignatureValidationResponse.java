package com.example.DocsSignatureAppBE.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response for signature validation
 * Validates coordinates are within acceptable bounds
 */
@Schema(description = "Signature validation result with coordinate validation")
public class SignatureValidationResponse {

    @Schema(description = "Whether signature is valid", example = "true")
    private Boolean isValid;

    @Schema(description = "Validation message", example = "Coordinates valid")
    private String message;

    @Schema(description = "X coordinate valid", example = "true")
    private Boolean xValid;

    @Schema(description = "Y coordinate valid", example = "true")
    private Boolean yValid;

    @Schema(description = "Page number valid", example = "true")
    private Boolean pageValid;

    @Schema(description = "Signature data present", example = "true")
    private Boolean signatureDataPresent;

    @Schema(description = "Recommended X coordinate if out of bounds", example = "25.5")
    private Float recommendedX;

    @Schema(description = "Recommended Y coordinate if out of bounds", example = "75.3")
    private Float recommendedY;

    @Schema(description = "Error details if invalid", example = "X coordinate out of bounds")
    private String errorDetails;

    // Constructors
    public SignatureValidationResponse() {}

    public SignatureValidationResponse(Boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    public SignatureValidationResponse(Boolean isValid, String message, Boolean xValid, Boolean yValid,
                                      Boolean pageValid, Boolean signatureDataPresent) {
        this.isValid = isValid;
        this.message = message;
        this.xValid = xValid;
        this.yValid = yValid;
        this.pageValid = pageValid;
        this.signatureDataPresent = signatureDataPresent;
    }

    // Getters and Setters
    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean valid) {
        isValid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getXValid() {
        return xValid;
    }

    public void setXValid(Boolean xValid) {
        this.xValid = xValid;
    }

    public Boolean getYValid() {
        return yValid;
    }

    public void setYValid(Boolean yValid) {
        this.yValid = yValid;
    }

    public Boolean getPageValid() {
        return pageValid;
    }

    public void setPageValid(Boolean pageValid) {
        this.pageValid = pageValid;
    }

    public Boolean getSignatureDataPresent() {
        return signatureDataPresent;
    }

    public void setSignatureDataPresent(Boolean signatureDataPresent) {
        this.signatureDataPresent = signatureDataPresent;
    }

    public Float getRecommendedX() {
        return recommendedX;
    }

    public void setRecommendedX(Float recommendedX) {
        this.recommendedX = recommendedX;
    }

    public Float getRecommendedY() {
        return recommendedY;
    }

    public void setRecommendedY(Float recommendedY) {
        this.recommendedY = recommendedY;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
}

