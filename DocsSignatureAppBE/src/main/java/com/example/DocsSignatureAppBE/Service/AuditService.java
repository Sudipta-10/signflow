package com.example.DocsSignatureAppBE.Service;

import com.example.DocsSignatureAppBE.Entity.AuditLog;
import com.example.DocsSignatureAppBE.Repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String username, String details, String documentId, Long userId, String action) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setDetails(details);
        log.setDocumentId(documentId);
        log.setUserId(userId);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditHistory(String documentId) {
        return auditLogRepository.findByDocumentId(documentId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public void logAction(String action, String ipAddress, String s) {
    }

    public void logAction(String username, String s) {
    }

    public List<AuditLog> getLogById(Long id) {
        return auditLogRepository.findById(id).map(List::of).orElse(List.of());
    }

    public List<AuditLog> getUserAuditHistory(String userId) {
        return auditLogRepository.findByUserId(Long.valueOf(userId));
    }
}
