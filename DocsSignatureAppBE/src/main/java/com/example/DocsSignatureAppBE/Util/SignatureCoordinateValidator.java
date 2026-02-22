package com.example.DocsSignatureAppBE.Util;

import com.example.DocsSignatureAppBE.DTO.SignatureValidationResponse;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating signature coordinates
 * Ensures coordinates are within acceptable bounds for PDF rendering
 */
@Component
public class SignatureCoordinateValidator {

    // Constants for coordinate bounds
    private static final float MIN_COORDINATE = 0f;
    private static final float MAX_COORDINATE = 100f;
    private static final float MARGIN = 5f; // 5% margin from edges
    private static final float MIN_SIGNATURE_WIDTH = 50f; // Minimum signature width
    private static final float MIN_SIGNATURE_HEIGHT = 30f; // Minimum signature height
    private static final float MAX_SIGNATURE_WIDTH = 300f; // Maximum signature width
    private static final float MAX_SIGNATURE_HEIGHT = 150f; // Maximum signature height

    /**
     * Validate signature placement coordinates
     */
    public SignatureValidationResponse validateCoordinates(Float xCoordinate, Float yCoordinate,
                                                          Integer pageNumber, String signatureData) {
        SignatureValidationResponse response = new SignatureValidationResponse();

        // Check if signature data is present
        boolean hasSignatureData = signatureData != null && !signatureData.isEmpty();
        response.setSignatureDataPresent(hasSignatureData);

        // Validate X coordinate
        boolean xValid = isCoordinateValid(xCoordinate);
        response.setXValid(xValid);

        // Validate Y coordinate
        boolean yValid = isCoordinateValid(yCoordinate);
        response.setYValid(yValid);

        // Validate page number
        boolean pageValid = isPageNumberValid(pageNumber);
        response.setPageValid(pageValid);

        // Overall validation
        boolean isValid = xValid && yValid && pageValid && hasSignatureData;
        response.setIsValid(isValid);

        // Generate message and recommendations
        if (isValid) {
            response.setMessage("Coordinates are valid and within acceptable bounds");
        } else {
            StringBuilder errorDetails = new StringBuilder();

            if (!xValid) {
                errorDetails.append("X coordinate out of bounds. ");
                response.setRecommendedX(clampCoordinate(xCoordinate));
            }

            if (!yValid) {
                errorDetails.append("Y coordinate out of bounds. ");
                response.setRecommendedY(clampCoordinate(yCoordinate));
            }

            if (!pageValid) {
                errorDetails.append("Invalid page number. ");
            }

            if (!hasSignatureData) {
                errorDetails.append("Signature data is missing. ");
            }

            response.setMessage("Validation failed. See errorDetails for specifics.");
            response.setErrorDetails(errorDetails.toString().trim());
        }

        return response;
    }

    /**
     * Validate signature dimensions
     */
    public boolean validateSignatureDimensions(Float width, Float height) {
        if (width == null || height == null) {
            return true; // Optional, so null is acceptable
        }

        return (width >= MIN_SIGNATURE_WIDTH && width <= MAX_SIGNATURE_WIDTH) &&
               (height >= MIN_SIGNATURE_HEIGHT && height <= MAX_SIGNATURE_HEIGHT);
    }

    /**
     * Check if coordinate is within valid bounds
     */
    private boolean isCoordinateValid(Float coordinate) {
        return coordinate != null &&
               coordinate >= MIN_COORDINATE &&
               coordinate <= MAX_COORDINATE;
    }

    /**
     * Check if page number is valid (minimum 1)
     */
    private boolean isPageNumberValid(Integer pageNumber) {
        return pageNumber != null && pageNumber >= 1;
    }

    /**
     * Clamp coordinate to valid range
     */
    private Float clampCoordinate(Float coordinate) {
        if (coordinate == null) {
            return 50f; // Default to center
        }
        return Math.max(MIN_COORDINATE, Math.min(MAX_COORDINATE, coordinate));
    }

    /**
     * Check if coordinate is near edge (within margin)
     */
    public boolean isNearEdge(Float xCoordinate, Float yCoordinate) {
        return (xCoordinate < MARGIN || xCoordinate > (MAX_COORDINATE - MARGIN)) ||
               (yCoordinate < MARGIN || yCoordinate > (MAX_COORDINATE - MARGIN));
    }

    /**
     * Check if coordinates overlap
     */
    public boolean coordinatesOverlap(Float x1, Float y1, Float x2, Float y2, Float threshold) {
        // Threshold in percentage (e.g., 10 means 10% overlap allowed)
        float distance = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        return distance < threshold;
    }

    /**
     * Normalize coordinates (ensure they're within bounds)
     */
    public Float normalizeCoordinate(Float coordinate) {
        return clampCoordinate(coordinate);
    }

    /**
     * Convert pixel-based coordinates to percentage
     * Used when converting from pixel position to percentage on PDF
     */
    public Float pixelToPercentage(Float pixelValue, Float containerSize) {
        if (containerSize == null || containerSize == 0) {
            return 0f;
        }
        return (pixelValue / containerSize) * 100f;
    }

    /**
     * Convert percentage-based coordinates to pixels
     * Used when displaying signature placeholder on PDF
     */
    public Float percentageToPixel(Float percentageValue, Float containerSize) {
        if (containerSize == null) {
            return 0f;
        }
        return (percentageValue / 100f) * containerSize;
    }

    /**
     * Calculate relative position within container
     */
    public Float calculateRelativePosition(Float absolutePixelPosition, Float containerStartPixel, Float containerSize) {
        if (containerSize == null || containerSize == 0) {
            return 0f;
        }
        float relativePixelPosition = absolutePixelPosition - containerStartPixel;
        return pixelToPercentage(relativePixelPosition, containerSize);
    }

    /**
     * Get safe coordinates with minimum margin from edges
     */
    public Float[] getSafeCoordinates(Float xCoordinate, Float yCoordinate) {
        Float safeX = clampCoordinate(xCoordinate);
        Float safeY = clampCoordinate(yCoordinate);

        // Ensure minimum margin
        safeX = Math.max(MARGIN, Math.min(MAX_COORDINATE - MARGIN, safeX));
        safeY = Math.max(MARGIN, Math.min(MAX_COORDINATE - MARGIN, safeY));

        return new Float[]{safeX, safeY};
    }
}

