package com.example.DocsSignatureAppBE.Controller;

import com.example.DocsSignatureAppBE.DTO.DocumentDetailDto;
import com.example.DocsSignatureAppBE.DTO.DocumentDto;
import com.example.DocsSignatureAppBE.DTO.SignatureRequestDto;
import com.example.DocsSignatureAppBE.Service.DocumentService;
import com.example.DocsSignatureAppBE.Service.PdfSignatureService;
import com.example.DocsSignatureAppBE.Service.SignatureRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document upload and retrieval APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

    private final DocumentService documentService;
    private final PdfSignatureService pdfSignatureService;
    private final SignatureRequestService signatureRequestService;

    public DocumentController(DocumentService documentService, PdfSignatureService pdfSignatureService, SignatureRequestService signatureRequestService) throws Exception {
        this.documentService = documentService;
        this.pdfSignatureService = pdfSignatureService;
        this.signatureRequestService = signatureRequestService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a document", description = "Upload a PDF document and save metadata to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or title"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<DocumentDto> uploadDocument(
            @Parameter(description = "PDF file to upload")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document title")
            @RequestParam("title") String title) throws Exception {
        DocumentDto documentDto = documentService.uploadDocument(file, title);
        return ResponseEntity.ok(documentDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieve a specific document by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<DocumentDetailDto> getDocumentById(
            @Parameter(description = "Document ID")
            @PathVariable Long id) {
        DocumentDetailDto documentDto = documentService.getDocumentById(id);
        return ResponseEntity.ok(documentDto);
    }

    @GetMapping
    @Operation(summary = "Get all documents", description = "Retrieve all documents from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<DocumentDto>> getAllDocuments() {
        List<DocumentDto> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/{id}/send-request")
    @Operation(summary = "Send signature requests", description = "Simulate sending emails to receivers for several people signature flow.")
    public ResponseEntity<?> sendSignatureRequest(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @RequestBody SignatureRequestDto request
    ) {
        signatureRequestService.processSignatureRequest(id, request);
        return ResponseEntity.ok().body("Signature request sent successfully");
    }

    @PostMapping("/{id}/finalize-signature")
    @Operation(summary = "Finalize signature and produce signed PDF")
    public ResponseEntity<?> finalizeSignature(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @RequestBody com.example.DocsSignatureAppBE.DTO.FinalizeSignatureRequestDto request
    ) throws Exception {
        DocumentDetailDto doc = documentService.getDocumentById(id);
        String inputPath = doc.getFilePath();

        // Decode base64 image
        String imageBase64 = request.getSignatureBase64();
        File tempImageFile = null;
        String imagePathForEmbed = null;

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            if (imageBase64.contains(",")) {
                imageBase64 = imageBase64.split(",")[1];
            }
            tempImageFile = File.createTempFile("temp_sig_", ".png");
            byte[] imageBytes = java.util.Base64.getDecoder().decode(imageBase64);
            java.nio.file.Files.write(tempImageFile.toPath(), imageBytes);
            imagePathForEmbed = tempImageFile.getAbsolutePath();
        }

        // Embed images (multiple)
        File stamped = pdfSignatureService.embedImages(inputPath, imagePathForEmbed, request.getPositions());
        
        // Clean up temp image
        if (tempImageFile != null && tempImageFile.exists()) {
            tempImageFile.delete();
        }

        // Sign PDF (crypto)
        File signed = pdfSignatureService.signPdf(stamped.getAbsolutePath(), "Signer", "Approved", "Location");

        // Update document
        documentService.markDocumentAsSigned(id, signed.getAbsolutePath());

        return ResponseEntity.ok().body("Signed file stored at: " + signed.getAbsolutePath());
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download original PDF")
    public ResponseEntity<FileSystemResource> downloadOriginal(@PathVariable Long id) {
        DocumentDetailDto doc = documentService.getDocumentById(id);
        String filePath = doc.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", file.getName());
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/{id}/download-signed")

    @Operation(summary = "Download final signed PDF")
    public ResponseEntity<FileSystemResource> downloadSigned(@PathVariable Long id) {
        DocumentDetailDto doc = documentService.getDocumentById(id);
        String signedPath = doc.getFinalSignedFilePath();
        if (signedPath == null || signedPath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(signedPath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", file.getName());
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
