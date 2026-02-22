package com.example.DocsSignatureAppBE.Service;

import com.example.DocsSignatureAppBE.DTO.DocumentDto;
import com.example.DocsSignatureAppBE.DTO.DocumentDetailDto;
import com.example.DocsSignatureAppBE.Entity.Document;
import com.example.DocsSignatureAppBE.Entity.User;
import com.example.DocsSignatureAppBE.Repository.DocumentRepository;
import com.example.DocsSignatureAppBE.Repository.UserRepository;
import com.example.DocsSignatureAppBE.Security.FileStorageUtil;
import com.example.DocsSignatureAppBE.Exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository, FileStorageUtil fileStorageUtil) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.fileStorageUtil = fileStorageUtil;
    }

    /**
     * Upload document for the current authenticated user
     */
    public DocumentDto uploadDocument(MultipartFile file, String title) throws Exception {
        User currentUser = getCurrentUser();

        String filePath = fileStorageUtil.storeFile(file);
        Document document = new Document();
        document.setFilename(title);
        document.setFilePath(filePath);
        document.setOriginalFilename(file.getOriginalFilename());
        document.setFileSize(file.getSize());
        document.setMimeType(file.getContentType());
        document.setUploadedBy(currentUser);
        document.setContent("File stored at: " + filePath);

        Document savedDocument = documentRepository.save(document);
        return convertToDto(savedDocument);
    }

    /**
     * Get document by ID with full details
     */
    public DocumentDetailDto getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + id));

        // Verify user has access to this document
        User currentUser = getCurrentUser();
        if (!document.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You do not have access to this document");
        }

        return convertToDetailDto(document);
    }

    /**
     * Get all documents (admin only) - returns all documents
     */
    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get documents for the current logged-in user
     */
    public List<DocumentDto> getUserDocuments() {
        User currentUser = getCurrentUser();
        return documentRepository.findByUploadedBy(currentUser).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get documents for a specific user (by user ID)
     */
    public List<DocumentDto> getDocumentsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return documentRepository.findByUploadedBy(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get document preview/details for frontend
     */
    public DocumentDetailDto getDocumentPreview(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        User currentUser = getCurrentUser();
        if (!document.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You do not have access to this document");
        }

        return convertToDetailDto(document);
    }

    /**
     * Delete document
     */
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        User currentUser = getCurrentUser();
        if (!document.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You do not have access to delete this document");
        }

        documentRepository.delete(document);
    }

    /**
     * Update document details
     */
    public DocumentDto updateDocument(Long documentId, String title, String description) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        User currentUser = getCurrentUser();
        if (!document.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You do not have access to update this document");
        }

        document.setFilename(title);
        document.setDescription(description);
        Document updatedDocument = documentRepository.save(document);

        return convertToDto(updatedDocument);
    }

    /**
     * Mark document as signed
     */
    public void markDocumentAsSigned(Long documentId, String signedFilePath) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        document.setIsSigned(true);
        document.setFileStatus(com.example.DocsSignatureAppBE.Entity.FileStatus.SIGNED);
        document.setFinalSignedFilePath(signedFilePath);
        document.setSignedAt(java.time.LocalDateTime.now());
        document.setSignedBy("Signer"); // Assuming a simple generic signer string for now
        documentRepository.save(document);
    }

    /**
     * Mark document as awaiting signatures
     */
    public void markDocumentAsAwaitingSignatures(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        document.setFileStatus(com.example.DocsSignatureAppBE.Entity.FileStatus.PROCESSING);
        documentRepository.save(document);
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
     * Convert Document entity to DocumentDto
     */
    private DocumentDto convertToDto(Document document) {
        return new DocumentDto(
                document.getId(),
                document.getFilename(),
                document.getOriginalFilename(),
                document.getFileSize(),
                document.getMimeType(),
                document.getUploadDate(),
                document.getIsSigned(),
                document.getFileStatus() != null ? document.getFileStatus().toString() : null
        );
    }

    /**
     * Convert Document entity to DocumentDetailDto (includes more details)
     */
    private DocumentDetailDto convertToDetailDto(Document document) {
        return new DocumentDetailDto(
                document.getId(),
                document.getFilename(),
                document.getOriginalFilename(),
                document.getFilePath(),
                document.getFileSize(),
                document.getMimeType(),
                document.getDescription(),
                document.getUploadDate(),
                document.getIsSigned(),
                document.getIsSignatureRequired(),
                document.getFileStatus().toString(),
                document.getUploadedBy().getUsername(),
                document.getFinalSignedFilePath()
        );
    }
}
