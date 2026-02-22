package com.example.DocsSignatureAppBE.Entity;

/**
 * Enum representing the status of a file during its lifecycle
 */
public enum FileStatus {
    /**
     * File has been uploaded and is waiting for processing or signature
     */
    PENDING("Pending"),

    /**
     * File has been sent out and we are waiting for signatures
     */
    AWAITING_SIGNATURES("Awaiting Signatures"),

    /**
     * File is currently being processed or reviewed
     */
    PROCESSING("Processing"),

    /**
     * File has been successfully signed
     */
    SIGNED("Signed"),

    /**
     * File is archived and no longer active
     */
    ARCHIVED("Archived"),

    /**
     * File has been rejected or failed processing
     */
    REJECTED("Rejected"),

    /**
     * File upload/processing has been cancelled
     */
    CANCELLED("Cancelled");

    private final String displayName;

    FileStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get FileStatus from string value
     * @param status the status string
     * @return FileStatus enum value
     */
    public static FileStatus fromString(String status) {
        try {
            return FileStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}

