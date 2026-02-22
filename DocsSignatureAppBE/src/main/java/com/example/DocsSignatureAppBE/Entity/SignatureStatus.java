package com.example.DocsSignatureAppBE.Entity;

/**
 * Enum representing status of a signature
 */
public enum SignatureStatus {
    /**
     * Signature placeholder created, waiting for signer
     */
    PENDING("Pending"),

    /**
     * Signature completed by signer
     */
    SIGNED("Signed"),

    /**
     * Signer rejected the signature request
     */
    REJECTED("Rejected"),

    /**
     * Signature request expired
     */
    EXPIRED("Expired"),

    /**
     * Signature cancelled by document owner
     */
    CANCELLED("Cancelled");

    private final String displayName;

    SignatureStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SignatureStatus fromString(String status) {
        try {
            return SignatureStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}

