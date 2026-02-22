package com.example.DocsSignatureAppBE.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "public_signature_link", indexes = {@Index(name = "idx_token_hash", columnList = "token_hash")})
public class PublicSignatureLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "one_time")
    private Boolean oneTime = true;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used_by_ip")
    private String usedByIp;

    @Column(name = "status")
    private String status = "ACTIVE";

    @Column(name = "allow_visual_sign")
    private Boolean allowVisualSign = true;

    @Column(name = "allow_download_after_sign")
    private Boolean allowDownloadAfterSign = true;

    @Column(name = "access_count")
    private Integer accessCount = 0;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getOneTime() { return oneTime; }
    public void setOneTime(Boolean oneTime) { this.oneTime = oneTime; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public String getUsedByIp() { return usedByIp; }
    public void setUsedByIp(String usedByIp) { this.usedByIp = usedByIp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getAllowVisualSign() { return allowVisualSign; }
    public void setAllowVisualSign(Boolean allowVisualSign) { this.allowVisualSign = allowVisualSign; }

    public Boolean getAllowDownloadAfterSign() { return allowDownloadAfterSign; }
    public void setAllowDownloadAfterSign(Boolean allowDownloadAfterSign) { this.allowDownloadAfterSign = allowDownloadAfterSign; }

    public Integer getAccessCount() { return accessCount; }
    public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }

    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}

