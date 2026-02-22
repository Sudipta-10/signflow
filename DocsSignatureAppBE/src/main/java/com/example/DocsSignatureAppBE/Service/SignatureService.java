package com.example.DocsSignatureAppBE.Service;

import com.example.DocsSignatureAppBE.DTO.SignatureDto;
import com.example.DocsSignatureAppBE.DTO.SignaturePlacementRequest;
import com.example.DocsSignatureAppBE.DTO.SignatureValidationResponse;
import com.example.DocsSignatureAppBE.DTO.SignatureDtoWithImage;
import com.example.DocsSignatureAppBE.Entity.*;
import com.example.DocsSignatureAppBE.Repository.DocumentRepository;
import com.example.DocsSignatureAppBE.Repository.SignatureRepository;
import com.example.DocsSignatureAppBE.Repository.UserRepository;
import com.example.DocsSignatureAppBE.Exception.ResourceNotFoundException;
import com.example.DocsSignatureAppBE.Util.SignatureCoordinateValidator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SignatureService {

    private final SignatureRepository signatureRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SignatureCoordinateValidator coordinateValidator;
    private final SignatureStorageService storageService;

    public SignatureService(SignatureRepository signatureRepository, DocumentRepository documentRepository,
                           UserRepository userRepository, SignatureCoordinateValidator coordinateValidator,
                           SignatureStorageService storageService) {
        this.signatureRepository = signatureRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.coordinateValidator = coordinateValidator;
        this.storageService = storageService;
    }

    /**
     * Add a signature placeholder to a document
     */
    public SignatureDto addSignaturePlaceholder(Long documentId, Long signerId, Float xCoordinate,
                                                Float yCoordinate, Integer pageNumber, String signatureType) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User signer = userRepository.findById(signerId)
                .orElseThrow(() -> new ResourceNotFoundException("Signer not found"));

        // Verify document owner is adding signature
        User currentUser = getCurrentUser();
        if (!document.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Only document owner can add signatures");
        }

        // Check if signer already has a signature on this document
        if (signatureRepository.findByDocumentAndSigner(document, signer).isPresent()) {
            throw new ResourceNotFoundException("Signer already has a signature on this document");
        }

        Signature signature = new Signature();
        signature.setDocument(document);
        signature.setSigner(signer);
        signature.setXCoordinate(xCoordinate);
        signature.setYCoordinate(yCoordinate);
        signature.setPageNumber(pageNumber);
        signature.setStatus(SignatureStatus.PENDING);

        if (signatureType != null) {
            signature.setSignatureType(SignatureType.fromString(signatureType));
        }

        Signature savedSignature = signatureRepository.save(signature);
        return convertToDto(savedSignature);
    }

    /**
     * Get all signatures for a document
     */
    public List<SignatureDto> getDocumentSignatures(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        return signatureRepository.findByDocumentOrderByPageNumberAscXCoordinateAsc(document)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get pending signatures for the current user
     */
    public List<SignatureDto> getPendingSignatures() {
        User currentUser = getCurrentUser();

        return signatureRepository.findBySignerAndStatusOrderByCreatedDateDesc(currentUser, SignatureStatus.PENDING)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all signatures for a specific signer
     */
    public List<SignatureDto> getSignerSignatures(Long signerId) {
        User signer = userRepository.findById(signerId)
                .orElseThrow(() -> new ResourceNotFoundException("Signer not found"));

        return signatureRepository.findBySigner(signer)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Sign a signature placeholder
     */
    public SignatureDto signDocument(Long signatureId, String signatureData, String reason, String location) {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Signature not found"));

        User currentUser = getCurrentUser();

        // Verify current user is the signer
        if (!signature.getSigner().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You are not the designated signer for this signature");
        }

        // Verify signature is still pending
        if (!signature.isPending()) {
            throw new ResourceNotFoundException("Signature has already been processed");
        }

        signature.sign(signatureData);
        signature.setReason(reason);
        signature.setLocation(location);

        Signature updatedSignature = signatureRepository.save(signature);

        // Update document's isSigned flag if all signatures are complete
        updateDocumentSignatureStatus(signature.getDocument().getId());

        return convertToDto(updatedSignature);
    }

    /**
     * Reject a signature request
     */
    public SignatureDto rejectSignature(Long signatureId, String reason) {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Signature not found"));

        User currentUser = getCurrentUser();

        // Verify current user is the signer
        if (!signature.getSigner().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You are not the designated signer");
        }

        signature.reject(reason);
        Signature updatedSignature = signatureRepository.save(signature);

        return convertToDto(updatedSignature);
    }

    /**
     * Sign document with drag-and-drop coordinates from UI
     * Handles coordinate validation, signature storage, and status updates
     */
    public SignatureDto signWithDragDrop(SignaturePlacementRequest request) throws IOException {
        Signature signature = signatureRepository.findById(request.getSignatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Signature not found"));

        User currentUser = getCurrentUser();

        // Verify current user is the signer
        if (!signature.getSigner().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You are not the designated signer for this signature");
        }

        // Verify signature is still pending
        if (!signature.isPending()) {
            throw new ResourceNotFoundException("Signature has already been processed");
        }

        // Validate coordinates
        SignatureValidationResponse validation = validateSignaturePlacement(request);
        if (!validation.getIsValid()) {
            throw new IllegalArgumentException("Signature coordinates validation failed: " + validation.getErrorDetails());
        }

        // Update coordinates from drag-drop UI
        signature.setXCoordinate(request.getXCoordinate());
        signature.setYCoordinate(request.getYCoordinate());
        if (request.getPageNumber() != null) {
            signature.setPageNumber(request.getPageNumber());
        }

        // Store signature image
        String signatureImagePath = storageService.storeSignatureImage(request.getSignatureData(), signature.getId());
        signature.setSignatureImagePath(signatureImagePath);

        // Store signature dimensions if provided
        if (request.getWidth() != null) {
            signature.setWidth(request.getWidth());
        }
        if (request.getHeight() != null) {
            signature.setHeight(request.getHeight());
        }

        // Sign the document
        signature.sign(request.getSignatureData());
        signature.setReason(request.getReason());
        signature.setLocation(request.getLocation());
        if (request.getNotes() != null) {
            signature.setNotes(request.getNotes());
        }

        if (request.getSignatureType() != null) {
            signature.setSignatureType(SignatureType.fromString(request.getSignatureType()));
        }

        Signature updatedSignature = signatureRepository.save(signature);

        // Update document's isSigned flag if all signatures are complete
        updateDocumentSignatureStatus(signature.getDocument().getId());

        return convertToDto(updatedSignature);
    }

    /**
     * Validate signature placement coordinates
     */
    public SignatureValidationResponse validateSignaturePlacement(SignaturePlacementRequest request) {
        SignatureValidationResponse response = coordinateValidator.validateCoordinates(
                request.getXCoordinate(),
                request.getYCoordinate(),
                request.getPageNumber(),
                request.getSignatureData()
        );

        // Additional validation for signature dimensions if provided
        if (request.getWidth() != null && request.getHeight() != null) {
            boolean dimensionsValid = coordinateValidator.validateSignatureDimensions(
                    request.getWidth(),
                    request.getHeight()
            );
            if (!dimensionsValid) {
                response.setIsValid(false);
                response.setErrorDetails("Signature dimensions out of acceptable range");
            }
        }

        return response;
    }

    /**
     * Validate coordinates only (without signing)
     * Used for real-time validation in frontend
     */
    public SignatureValidationResponse validateCoordinatesOnly(Float xCoordinate, Float yCoordinate,
                                                               Integer pageNumber, String signatureData) {
        return coordinateValidator.validateCoordinates(xCoordinate, yCoordinate, pageNumber, signatureData);
    }

    /**
     * Update signature coordinates (before signing)
     * For drag-and-drop repositioning
     */
    public SignatureDto updateSignatureCoordinatesWithValidation(Long signatureId, Float xCoordinate,
                                                                  Float yCoordinate, Integer pageNumber) {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Signature not found"));

        User currentUser = getCurrentUser();

        // Verify current user is the signer or document owner
        if (!signature.getSigner().getId().equals(currentUser.getId()) &&
            !signature.getDocument().getUploadedBy().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You do not have permission to update this signature");
        }

        // Can only update if pending
        if (!signature.isPending()) {
            throw new ResourceNotFoundException("Cannot update a signature that has already been processed");
        }

        // Validate new coordinates
        SignatureValidationResponse validation = coordinateValidator.validateCoordinates(
                xCoordinate, yCoordinate, pageNumber, ""
        );

        if (!validation.getXValid() || !validation.getYValid() || !validation.getPageValid()) {
            throw new IllegalArgumentException("Invalid coordinates: " + validation.getErrorDetails());
        }

        // Check if coordinates are too close to edge
        if (coordinateValidator.isNearEdge(xCoordinate, yCoordinate)) {
            // Allow but could log warning
        }

        signature.setXCoordinate(xCoordinate);
        signature.setYCoordinate(yCoordinate);
        signature.setPageNumber(pageNumber);

        Signature updatedSignature = signatureRepository.save(signature);
        return convertToDto(updatedSignature);
    }

    /**
     * Get signature with rendered image
     */
    public SignatureDtoWithImage getSignatureWithImage(Long signatureId) throws IOException {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Signature not found"));

        SignatureDtoWithImage dto = new SignatureDtoWithImage(convertToDto(signature));

        if (signature.getSignatureImagePath() != null) {
            String imageData = storageService.getSignatureImageAsBase64(signature.getSignatureImagePath());
            dto.setSignatureImageData(imageData);
        }

        return dto;
    }

    /**
     * Get all signatures with images for a document
     */
    public List<SignatureDtoWithImage> getDocumentSignaturesWithImages(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        List<Signature> signatures = signatureRepository.findByDocumentOrderByPageNumberAscXCoordinateAsc(document);

        return signatures.stream()
                .map(sig -> {
                    try {
                        SignatureDtoWithImage dto = new SignatureDtoWithImage(convertToDto(sig));
                        if (sig.getSignatureImagePath() != null) {
                            String imageData = storageService.getSignatureImageAsBase64(sig.getSignatureImagePath());
                            dto.setSignatureImageData(imageData);
                        }
                        return dto;
                    } catch (IOException e) {
                        throw new RuntimeException("Error loading signature image: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Get signature by ID
     */
    public SignatureDto getSignatureById(Long signatureId) {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Signature not found"));

        return convertToDto(signature);
    }

    /**
     * Check if all signatures for a document are complete
     */
    public boolean areAllSignaturesComplete(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        List<Signature> signatures = signatureRepository.findByDocument(document);

        if (signatures.isEmpty()) {
            return true;
        }

        return signatures.stream().allMatch(sig -> sig.isSigned());
    }

    /**
     * Get signature statistics for a document
     */
    public SignatureStatsDto getSignatureStats(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        List<Signature> allSignatures = signatureRepository.findByDocument(document);
        long completedCount = allSignatures.stream().filter(Signature::isSigned).count();
        long pendingCount = allSignatures.stream().filter(Signature::isPending).count();

        return new SignatureStatsDto(
            allSignatures.size(),
            (int) completedCount,
            (int) pendingCount,
            areAllSignaturesComplete(documentId)
        );
    }

    /**
     * Update document signature status based on signature completion
     */
    private void updateDocumentSignatureStatus(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (areAllSignaturesComplete(documentId)) {
            document.setIsSigned(true);
            document.setFileStatus(FileStatus.SIGNED);
            documentRepository.save(document);
        }
    }

    /**
     * Get current authenticated user from security context
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    /**
     * Convert Signature entity to SignatureDto
     */
    private SignatureDto convertToDto(Signature signature) {
        return new SignatureDto(
                signature.getId(),
                signature.getDocument().getId(),
                signature.getSigner().getUsername(),
                signature.getXCoordinate(),
                signature.getYCoordinate(),
                signature.getPageNumber(),
                signature.getSignatureType().name(),
                signature.getStatus().name(),
                signature.getSignedDate(),
                signature.getReason(),
                signature.getLocation(),
                signature.getWidth(),
                signature.getHeight()
        );
    }

    public SignatureDto updateSignatureLocation(Long signatureId, Float xCoordinate, Float yCoordinate, Integer pageNumber) {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Signature not found"));
        return null;
    }

    public void deleteSignature(Long signatureId) {}

    public void signDocument(Long id, String signatureData) {
        Long signatureId = null;
        Signature sig = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new RuntimeException("Signature not found"));

        sig.setStatus(SignatureStatus.SIGNED);
        sig.setSignatureData(signatureData);
        sig.setSignedDate(LocalDateTime.now());

        signatureRepository.save(sig);
    }

    /**
     * Inner DTO class for signature statistics
     */
    public static class SignatureStatsDto {
        private int totalSignatures;
        private int completedSignatures;
        private int pendingSignatures;
        private boolean allSigned;

        public SignatureStatsDto(int total, int completed, int pending, boolean allSigned) {
            this.totalSignatures = total;
            this.completedSignatures = completed;
            this.pendingSignatures = pending;
            this.allSigned = allSigned;
        }

        // Getters
        public int getTotalSignatures() { return totalSignatures; }
        public int getCompletedSignatures() { return completedSignatures; }
        public int getPendingSignatures() { return pendingSignatures; }
        public boolean isAllSigned() { return allSigned; }
    }
}

