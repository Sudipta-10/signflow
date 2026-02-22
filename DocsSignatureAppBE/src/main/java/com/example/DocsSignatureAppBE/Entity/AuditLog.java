package com.example.DocsSignatureAppBE.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action;
    private LocalDateTime timestamp;
    private String documentId;

    @Column(name="user_id")
    private Long userId;

    private String ipAddress;
    private String details;

    public AuditLog() {}

    public AuditLog(String username, String action, LocalDateTime timestamp, String documentId, Long userId, String ipAddress, String details) {
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
        this.documentId = documentId;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}
