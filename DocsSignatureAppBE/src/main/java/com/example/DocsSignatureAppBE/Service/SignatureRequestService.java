package com.example.DocsSignatureAppBE.Service;

import com.example.DocsSignatureAppBE.DTO.ReceiverDto;
import com.example.DocsSignatureAppBE.DTO.SignatureRequestDto;
import org.springframework.stereotype.Service;

@Service
public class SignatureRequestService {

    private final DocumentService documentService;

    public SignatureRequestService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void processSignatureRequest(Long documentId, SignatureRequestDto request) {
        // 1. Validate the document exists and user has access
        documentService.getDocumentById(documentId);

        // 2. Process receivers and simulate sending emails
        System.out.println("Processing signature request for Document ID: " + documentId);
        
        if (request.getSettings() != null) {
            System.out.println("Settings applied - Set Order: " + request.getSettings().isSetOrder());
            System.out.println("Settings applied - Password Protected: " + request.getReceivers().stream().anyMatch(ReceiverDto::isRequirePassword));
        }

        for (ReceiverDto receiver : request.getReceivers()) {
            System.out.println("==========================================");
            System.out.println("Simulating Email Notification...");
            System.out.println("To: " + receiver.getEmail());
            System.out.println("Subject: Signature Request - Please sign your document");
            System.out.println("Body: Hello " + receiver.getName() + ",\n\nYou have been requested to sign a document as a " + receiver.getRole() + ".");
            if (receiver.isRequirePassword()) {
                System.out.println("(Requires password to access)");
            }
            System.out.println("==========================================");
        }

        // 3. Update document status
        documentService.markDocumentAsAwaitingSignatures(documentId);
    }
}
