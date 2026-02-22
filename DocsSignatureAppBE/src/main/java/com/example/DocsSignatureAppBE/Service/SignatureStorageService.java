package com.example.DocsSignatureAppBE.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for handling signature image storage
 * Manages saving signature images and returning file paths
 */
@Service
public class SignatureStorageService {

    @Value("${signature.storage.dir:./signature-uploads}")
    private String signatureStorageDir;

    @Value("${signature.max-size:5242880}") // 5MB default
    private long maxSignatureSize;

    /**
     * Store signature image from base64 data
     */
    public String storeSignatureImage(String base64SignatureData, Long signatureId) throws IOException {
        // Create signature storage directory if not exists
        Path storageDirectoryPath = Paths.get(signatureStorageDir);
        if (!Files.exists(storageDirectoryPath)) {
            Files.createDirectories(storageDirectoryPath);
        }

        // Generate unique filename
        String filename = "signature_" + signatureId + "_" + UUID.randomUUID() + ".png";
        Path signatureFilePath = storageDirectoryPath.resolve(filename);

        // Decode base64 and validate size
        byte[] signatureBytes = decodeBase64SignatureData(base64SignatureData);

        if (signatureBytes.length > maxSignatureSize) {
            throw new IOException("Signature image exceeds maximum size of " + maxSignatureSize + " bytes");
        }

        // Write file to disk
        Files.write(signatureFilePath, signatureBytes);

        return signatureFilePath.toString();
    }

    /**
     * Retrieve signature image as base64
     */
    public String getSignatureImageAsBase64(String signatureImagePath) throws IOException {
        if (signatureImagePath == null || signatureImagePath.isEmpty()) {
            return null;
        }

        Path imagePath = Paths.get(signatureImagePath);
        if (!Files.exists(imagePath)) {
            throw new IOException("Signature image not found at: " + signatureImagePath);
        }

        byte[] fileBytes = Files.readAllBytes(imagePath);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(fileBytes);
    }

    /**
     * Delete signature image file
     */
    public void deleteSignatureImage(String signatureImagePath) throws IOException {
        if (signatureImagePath == null || signatureImagePath.isEmpty()) {
            return;
        }

        Path imagePath = Paths.get(signatureImagePath);
        if (Files.exists(imagePath)) {
            Files.delete(imagePath);
        }
    }

    /**
     * Decode base64 signature data
     * Handles both data URL format and plain base64
     */
    private byte[] decodeBase64SignatureData(String base64Data) throws IOException {
        if (base64Data == null || base64Data.isEmpty()) {
            throw new IOException("Signature data is empty");
        }

        try {
            // Remove data URL prefix if present (e.g., "data:image/png;base64,")
            String cleanData = base64Data;
            if (cleanData.contains(",")) {
                cleanData = cleanData.split(",")[1];
            }

            // Decode base64
            return Base64.getDecoder().decode(cleanData);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid base64 signature data: " + e.getMessage());
        }
    }

    /**
     * Validate signature image format
     */
    public boolean isValidSignatureFormat(String base64SignatureData) {
        if (base64SignatureData == null || base64SignatureData.isEmpty()) {
            return false;
        }

        // Check if it starts with valid image data URL
        return base64SignatureData.startsWith("data:image/") ||
               isValidBase64(base64SignatureData.split(",")[base64SignatureData.contains(",") ? 1 : 0]);
    }

    /**
     * Check if string is valid base64
     */
    private boolean isValidBase64(String data) {
        try {
            Base64.getDecoder().decode(data);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Store signature as SVG (vector format)
     */
    public String storeSignatureSVG(String svgData, Long signatureId) throws IOException {
        Path storageDirectoryPath = Paths.get(signatureStorageDir);
        if (!Files.exists(storageDirectoryPath)) {
            Files.createDirectories(storageDirectoryPath);
        }

        String filename = "signature_" + signatureId + "_" + UUID.randomUUID() + ".svg";
        Path signatureFilePath = storageDirectoryPath.resolve(filename);

        // Write SVG data directly
        Files.write(signatureFilePath, svgData.getBytes());

        return signatureFilePath.toString();
    }

    /**
     * Get file path for signature
     */
    public String getSignaturePath(Long signatureId) {
        return Paths.get(signatureStorageDir, "signature_" + signatureId + ".png").toString();
    }
}

