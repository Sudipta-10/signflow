package com.example.DocsSignatureAppBE.Service;

import com.example.DocsSignatureAppBE.DTO.FileUploadResponse;
import com.example.DocsSignatureAppBE.Entity.Document;
import com.example.DocsSignatureAppBE.Entity.User;
import com.example.DocsSignatureAppBE.Repository.DocumentRepository;
import com.example.DocsSignatureAppBE.Repository.UserRepository;
import com.example.DocsSignatureAppBE.Exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileUploadService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:52428800}")
    private long maxFileSize;

    @Value("${file.allowed-types:application/pdf}")
    private String allowedTypes;

    public FileUploadService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    public FileUploadResponse uploadFile(MultipartFile file, Long userId) throws IOException {
        // Validate file
        validateFile(file);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create upload directory if not exists
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // Generate unique filename
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + File.separator + uniqueFileName;

        // Save file to disk
        Files.copy(file.getInputStream(), Paths.get(filePath));

        // Create document entity
        Document document = new Document();
        document.setFilename(uniqueFileName);
        document.setFilePath(filePath);
        document.setContent( "String content not stored in DB for large files");
        Document savedDocument = documentRepository.save(document);

        // Create response
        FileUploadResponse response = new FileUploadResponse();
        response.setFileId(savedDocument.getId());
        response.setMessage("File uploaded successfully");

        return response;
    }

    public List<Document> getUserDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return documentRepository.findByUploadedBy(user);
    }

    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    public byte[] downloadFile(String filename) throws IOException {
        String filePath = uploadDir + File.separator + filename;
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("File not found");
        }

        return Files.readAllBytes(path);
    }

    public void deleteDocument(Long documentId, Long userId) throws IOException {
        Document document = getDocumentById(documentId);


        // Delete file from disk
        Path path = Paths.get(document.getFilePath());
        if (Files.exists(path)) {
            Files.delete(path);
        }

        // Delete document from database
        documentRepository.delete(document);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Validate file type
        if (!allowedTypes.contains(file.getContentType())) {
            throw new RuntimeException("File type " + file.getContentType() + " is not allowed. Only PDF files are accepted.");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Only PDF files are allowed");
        }
    }
}

