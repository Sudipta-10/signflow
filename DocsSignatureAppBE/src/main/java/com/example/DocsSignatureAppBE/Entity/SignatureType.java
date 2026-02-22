package com.example.DocsSignatureAppBE.Entity;

/**
 * Enum representing types of signatures
 */
public enum SignatureType {
    /**
     * Digital signature (drawn/mouse based)
     */
    DIGITAL("Digital Signature"),

    /**
     * Initials only (faster signing)
     */
    INITIALS("Initials"),

    /**
     * Image-based signature upload
     */
    IMAGE("Image Signature"),

    /**
     * Text-based signature
     */
    TEXT("Text Signature");

    private final String displayName;

    SignatureType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SignatureType fromString(String type) {
        try {
            return SignatureType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DIGITAL;
        }
    }
}

