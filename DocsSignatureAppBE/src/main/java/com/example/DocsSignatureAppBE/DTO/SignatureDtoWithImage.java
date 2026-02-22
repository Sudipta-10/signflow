package com.example.DocsSignatureAppBE.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for signature with embedded image data
 * Used for frontend display with rendered signature
 */
@Schema(description = "Signature with rendered image data")
public class SignatureDtoWithImage {

    private SignatureDto signature;

    @Schema(description = "Signature image as base64 data URI", example = "data:image/png;base64,...")
    private String signatureImageData;

    // Constructors
    public SignatureDtoWithImage() {}

    public SignatureDtoWithImage(SignatureDto signature) {
        this.signature = signature;
    }

    public SignatureDtoWithImage(SignatureDto signature, String signatureImageData) {
        this.signature = signature;
        this.signatureImageData = signatureImageData;
    }

    // Getters and Setters
    public SignatureDto getSignature() {
        return signature;
    }

    public void setSignature(SignatureDto signature) {
        this.signature = signature;
    }

    public String getSignatureImageData() {
        return signatureImageData;
    }

    public void setSignatureImageData(String signatureImageData) {
        this.signatureImageData = signatureImageData;
    }
}

