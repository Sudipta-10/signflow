package com.example.DocsSignatureAppBE.Repository;

import com.example.DocsSignatureAppBE.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByDocumentId(String documentId);
}
