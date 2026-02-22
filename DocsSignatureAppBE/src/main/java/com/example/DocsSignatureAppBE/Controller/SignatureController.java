package com.example.DocsSignatureAppBE.Controller;

import com.example.DocsSignatureAppBE.DTO.SignatureDto;
import com.example.DocsSignatureAppBE.DTO.SignaturePlacementRequest;
import com.example.DocsSignatureAppBE.DTO.SignatureValidationResponse;
import com.example.DocsSignatureAppBE.DTO.SignatureDtoWithImage;
import com.example.DocsSignatureAppBE.Service.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/signatures")
@Tag(name = "Signatures", description = "Signature management and placement APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class SignatureController {

    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    /**
     * Add a signature placeholder to a document
     */
    @PostMapping("/add-placeholder")
    @Operation(summary = "Add signature placeholder", description = "Add a signature placeholder on a document for a specific signer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Signature placeholder added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignatureDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Document or signer not found")
    })
    public ResponseEntity<SignatureDto> addSignaturePlaceholder(
            @Parameter(description = "Document ID")
            @RequestParam Long documentId,
            @Parameter(description = "Signer User ID")
            @RequestParam Long signerId,
            @Parameter(description = "X coordinate on PDF (0-100%)")
            @RequestParam Float xCoordinate,
            @Parameter(description = "Y coordinate on PDF (0-100%)")
            @RequestParam Float yCoordinate,
            @Parameter(description = "Page number (1-based)")
            @RequestParam Integer pageNumber,
            @Parameter(description = "Signature type (DIGITAL, INITIALS, IMAGE, TEXT)")
            @RequestParam(required = false, defaultValue = "DIGITAL") String signatureType) {

        SignatureDto signature = signatureService.addSignaturePlaceholder(
                documentId, signerId, xCoordinate, yCoordinate, pageNumber, signatureType);
        return ResponseEntity.status(HttpStatus.CREATED).body(signature);
    }

    /**
     * Get all signatures for a document
     */
    @GetMapping("/document/{documentId}")
    @Operation(summary = "Get document signatures", description = "Retrieve all signature placeholders for a document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signatures retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<List<SignatureDto>> getDocumentSignatures(
            @Parameter(description = "Document ID")
            @PathVariable Long documentId) {
        List<SignatureDto> signatures = signatureService.getDocumentSignatures(documentId);
        return ResponseEntity.ok(signatures);
    }

    /**
     * Get pending signatures for current user
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending signatures", description = "Retrieve all pending signatures waiting for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending signatures retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<SignatureDto>> getPendingSignatures() {
        List<SignatureDto> signatures = signatureService.getPendingSignatures();
        return ResponseEntity.ok(signatures);
    }

    /**
     * Get all signatures for a signer
     */
    @GetMapping("/signer/{signerId}")
    @Operation(summary = "Get signer signatures", description = "Retrieve all signatures for a specific signer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signatures retrieved"),
            @ApiResponse(responseCode = "404", description = "Signer not found")
    })
    public ResponseEntity<List<SignatureDto>> getSignerSignatures(
            @Parameter(description = "Signer User ID")
            @PathVariable Long signerId) {
        List<SignatureDto> signatures = signatureService.getSignerSignatures(signerId);
        return ResponseEntity.ok(signatures);
    }

    /**
     * Sign a document
     */
    @PostMapping("/{signatureId}/sign")
    @Operation(summary = "Sign document", description = "Complete a signature with drawn/uploaded signature data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document signed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid signature data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or not the designated signer"),
            @ApiResponse(responseCode = "404", description = "Signature not found")
    })
    public ResponseEntity<SignatureDto> signDocument(
            @Parameter(description = "Signature ID")
            @PathVariable Long signatureId,
            @Parameter(description = "Signature data (base64 encoded image or coordinates)")
            @RequestParam String signatureData,
            @Parameter(description = "Reason for signing")
            @RequestParam(required = false) String reason,
            @Parameter(description = "Location of signer")
            @RequestParam(required = false) String location) {

        SignatureDto signature = signatureService.signDocument(signatureId, signatureData, reason, location);
        return ResponseEntity.ok(signature);
    }

    /**
     * Reject a signature request
     */
    @PostMapping("/{signatureId}/reject")
    @Operation(summary = "Reject signature", description = "Reject a signature request with optional reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature rejected"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or not the designated signer"),
            @ApiResponse(responseCode = "404", description = "Signature not found")
    })
    public ResponseEntity<SignatureDto> rejectSignature(
            @Parameter(description = "Signature ID")
            @PathVariable Long signatureId,
            @Parameter(description = "Reason for rejection")
            @RequestParam(required = false) String reason) {

        SignatureDto signature = signatureService.rejectSignature(signatureId, reason);
        return ResponseEntity.ok(signature);
    }

    /**
     * Update signature location (before signing)
     */
    @PutMapping("/{signatureId}/location")
    @Operation(summary = "Update signature location", description = "Update signature coordinates before signing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Signature not found")
    })
    public ResponseEntity<SignatureDto> updateSignatureLocation(
            @Parameter(description = "Signature ID")
            @PathVariable Long signatureId,
            @Parameter(description = "New X coordinate")
            @RequestParam Float xCoordinate,
            @Parameter(description = "New Y coordinate")
            @RequestParam Float yCoordinate,
            @Parameter(description = "New page number")
            @RequestParam Integer pageNumber) {

        SignatureDto signature = signatureService.updateSignatureLocation(signatureId, xCoordinate, yCoordinate, pageNumber);
        return ResponseEntity.ok(signature);
    }

    /**
     * Delete a signature placeholder
     */
    @DeleteMapping("/{signatureId}")
    @Operation(summary = "Delete signature", description = "Delete a signature placeholder (pending only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Signature deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or not document owner"),
            @ApiResponse(responseCode = "404", description = "Signature not found")
    })
    public ResponseEntity<Void> deleteSignature(
            @Parameter(description = "Signature ID")
            @PathVariable Long signatureId) {

        signatureService.deleteSignature(signatureId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get signature by ID
     */
    @GetMapping("/{signatureId}")
    @Operation(summary = "Get signature", description = "Retrieve a specific signature by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature retrieved"),
            @ApiResponse(responseCode = "404", description = "Signature not found")
    })
    public ResponseEntity<SignatureDto> getSignatureById(
            @Parameter(description = "Signature ID")
            @PathVariable Long signatureId) {

        SignatureDto signature = signatureService.getSignatureById(signatureId);
        return ResponseEntity.ok(signature);
    }

    /**
     * Get signature statistics for a document
     */
    @GetMapping("/document/{documentId}/stats")
    @Operation(summary = "Get signature statistics", description = "Get signature completion statistics for a document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<?> getSignatureStats(
            @Parameter(description = "Document ID")
            @PathVariable Long documentId) {

        var stats = signatureService.getSignatureStats(documentId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Sign document with drag-and-drop UI coordinates
     * Validates coordinates, stores signature image, and updates document status
     */
    @PostMapping("/sign-with-drag-drop")
    @Operation(summary = "Sign with drag-drop UI",
            description = "Complete signature using drag-drop UI placement with coordinate validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document signed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or signature data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or not the designated signer"),
            @ApiResponse(responseCode = "404", description = "Signature not found")
    })
    public ResponseEntity<SignatureDto> signWithDragDrop(
            @Valid @RequestBody SignaturePlacementRequest request) throws IOException {

        SignatureDto signature = signatureService.signWithDragDrop(request);
        return ResponseEntity.ok(signature);
    }

    /**
     * Validate signature placement coordinates
     * Used for real-time validation in frontend
     */
    @PostMapping("/validate-placement")
    @Operation(summary = "Validate signature placement",
            description = "Validate that signature coordinates are within acceptable bounds")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation result"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SignatureValidationResponse> validatePlacement(
            @Parameter(description = "X coordinate (0-100%)")
            @RequestParam Float xCoordinate,
            @Parameter(description = "Y coordinate (0-100%)")
            @RequestParam Float yCoordinate,
            @Parameter(description = "Page number")
            @RequestParam(required = false) Integer pageNumber,
            @Parameter(description = "Signature data (optional)")
            @RequestParam(required = false) String signatureData) {

        SignatureValidationResponse validation = signatureService.validateCoordinatesOnly(
                xCoordinate, yCoordinate, pageNumber, signatureData);
        return ResponseEntity.ok(validation);
    }

    /**
     * Update signature coordinates with validation
     * Used for repositioning signature before final signing
     */
    @PutMapping("/{signatureId}/coordinates")
    @Operation(summary = "Update coordinates with validation",
            description = "Update signature coordinates and validate placement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coordinates updated"),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Signature not found")
    })
    public ResponseEntity<SignatureDto> updateCoordinatesWithValidation(
            @PathVariable Long signatureId,
            @RequestParam Float xCoordinate,
            @RequestParam Float yCoordinate,
            @RequestParam Integer pageNumber) {

        SignatureDto signature = signatureService.updateSignatureCoordinatesWithValidation(
                signatureId, xCoordinate, yCoordinate, pageNumber);
        return ResponseEntity.ok(signature);
    }

    /**
     * Get signature with rendered image
     * Returns signature data including the rendered image
     */
    @GetMapping("/{signatureId}/with-image")
    @Operation(summary = "Get signature with image",
            description = "Retrieve signature with rendered image data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature retrieved with image"),
            @ApiResponse(responseCode = "404", description = "Signature not found")
    })
    public ResponseEntity<SignatureDtoWithImage> getSignatureWithImage(
            @PathVariable Long signatureId) throws IOException {

        SignatureDtoWithImage signature = signatureService.getSignatureWithImage(signatureId);
        return ResponseEntity.ok(signature);
    }

    /**
     * Get all signatures for document with images
     * Returns all signatures including rendered images
     */
    @GetMapping("/document/{documentId}/with-images")
    @Operation(summary = "Get document signatures with images",
            description = "Retrieve all signatures for document with rendered images")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signatures retrieved with images"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<List<SignatureDtoWithImage>> getDocumentSignaturesWithImages(
            @PathVariable Long documentId) throws IOException {

        List<SignatureDtoWithImage> signatures = signatureService.getDocumentSignaturesWithImages(documentId);
        return ResponseEntity.ok(signatures);
    }

    @PutMapping("/{id}/sign")
    public ResponseEntity<?> signDocument(@PathVariable Long id,
                                          @RequestBody String signatureData) {
        signatureService.signDocument(id, signatureData);
        return ResponseEntity.ok("Document signed");
    }

}
