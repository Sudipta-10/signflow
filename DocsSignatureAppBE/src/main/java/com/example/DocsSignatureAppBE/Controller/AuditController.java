package com.example.DocsSignatureAppBE.Controller;

import com.example.DocsSignatureAppBE.Entity.AuditLog;
import com.example.DocsSignatureAppBE.Service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        List<AuditLog> logs = auditService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<AuditLog> getLogById(@PathVariable Long id) {
        AuditLog log = (AuditLog) auditService.getLogById(id);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<AuditLog>> getDocumentAuditHistory(@PathVariable String documentId) {
        return ResponseEntity.ok(auditService.getAuditHistory(documentId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getUserAuditHistory(@PathVariable String userId) {
        return ResponseEntity.ok(auditService.getUserAuditHistory(userId));
    }

}
