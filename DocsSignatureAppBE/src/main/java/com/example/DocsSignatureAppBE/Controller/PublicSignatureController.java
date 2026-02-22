package com.example.DocsSignatureAppBE.Controller;

import com.example.DocsSignatureAppBE.DTO.DocumentDetailDto;
import com.example.DocsSignatureAppBE.DTO.SignaturePlacementRequest;
import com.example.DocsSignatureAppBE.Entity.PublicSignatureLink;
import com.example.DocsSignatureAppBE.Service.DocumentService;
import com.example.DocsSignatureAppBE.Service.PublicSignatureService;
import com.example.DocsSignatureAppBE.Service.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping
@Tag(name = "PublicSignature", description = "Public signing links and flows")
public class PublicSignatureController {

    private final PublicSignatureService publicSignatureService;
    private final DocumentService documentService;
    private final SignatureService signatureService;

    public PublicSignatureController(PublicSignatureService publicSignatureService, DocumentService documentService, SignatureService signatureService) {
        this.publicSignatureService = publicSignatureService;
        this.documentService = documentService;
        this.signatureService = signatureService;
    }

    @PostMapping("/api/public-links")
    @Operation(summary = "Create a public signing link (authenticated)")
    public ResponseEntity<?> createPublicLink(@RequestParam Long documentId,
                                              @RequestParam(required = false) String recipientEmail,
                                              @RequestParam(required = false) Integer expiresInMinutes,
                                              @RequestParam(required = false) Boolean oneTime,
                                              @RequestParam(required = false) Long createdByUserId) {
        String token = publicSignatureService.createPublicLink(documentId, createdByUserId, recipientEmail, expiresInMinutes, oneTime);
        return ResponseEntity.ok().body("Public link created: /public/sign/" + token);
    }

    @GetMapping("/public/sign/{token}")
    @Operation(summary = "Open public signing page (public)")
    public ResponseEntity<?> openPublicSign(@PathVariable String token) {
        Optional<PublicSignatureLink> linkOpt = publicSignatureService.findByPlainToken(token);
        if (linkOpt.isEmpty()) return ResponseEntity.notFound().build();
        PublicSignatureLink link = linkOpt.get();
        DocumentDetailDto doc = documentService.getDocumentById(link.getDocument().getId());
        return ResponseEntity.ok().body(doc);
    }

    @PostMapping("/public/sign/{token}")
    @Operation(summary = "Submit signature via public link (public)")
    public ResponseEntity<?> submitPublicSignature(@PathVariable String token, @RequestBody SignaturePlacementRequest request, @RequestHeader(value = "X-Forwarded-For", required = false) String xff) {
        // Validate token and consume it
        boolean ok = publicSignatureService.consumeToken(token, xff);
        if (!ok) return ResponseEntity.status(403).body("Invalid or expired token");

        // Create a signature using existing services (must map request to existing signature placeholder or create new one)
        try {
            // Here we just use signWithDragDrop which expects an existing SignaturePlacementRequest DTO
            var result = signatureService.signWithDragDrop(request);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error signing document: " + ex.getMessage());
        }
    }
}

