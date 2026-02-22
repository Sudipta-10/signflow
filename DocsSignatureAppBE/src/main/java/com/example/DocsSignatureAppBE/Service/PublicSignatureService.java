package com.example.DocsSignatureAppBE.Service;

import com.example.DocsSignatureAppBE.Entity.Document;
import com.example.DocsSignatureAppBE.Entity.PublicSignatureLink;
import com.example.DocsSignatureAppBE.Entity.User;
import com.example.DocsSignatureAppBE.Exception.ResourceNotFoundException;
import com.example.DocsSignatureAppBE.Repository.DocumentRepository;
import com.example.DocsSignatureAppBE.Repository.PublicSignatureLinkRepository;
import com.example.DocsSignatureAppBE.Repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PublicSignatureService {

    private final PublicSignatureLinkRepository linkRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${publiclink.base.url:http://localhost:8080/public/sign}")
    private String publicBaseUrl;

    public PublicSignatureService(PublicSignatureLinkRepository linkRepository,
                                  DocumentRepository documentRepository,
                                  UserRepository userRepository,
                                  EmailService emailService) {
        this.linkRepository = linkRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    private String generateTokenPlain() {
        SecureRandom rnd = new SecureRandom();
        byte[] token = new byte[32];
        rnd.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }

    @Transactional
    public String createPublicLink(Long documentId, Long createdByUserId, String recipientEmail, Integer expiresInMinutes, Boolean oneTime) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User creator = userRepository.findById(createdByUserId).orElse(null);

        String plainToken = generateTokenPlain();
        String tokenHash = hashToken(plainToken);

        PublicSignatureLink link = new PublicSignatureLink();
        link.setDocument(doc);
        link.setCreatedAt(LocalDateTime.now());
        link.setCreatedBy(creator);
        link.setRecipientEmail(recipientEmail);
        link.setTokenHash(tokenHash);
        link.setOneTime(oneTime == null ? true : oneTime);
        if (expiresInMinutes != null && expiresInMinutes > 0) {
            link.setExpiresAt(LocalDateTime.now().plusMinutes(expiresInMinutes));
        }

        linkRepository.save(link);

        // send email to recipient with the plain token URL
        if (recipientEmail != null && !recipientEmail.isEmpty()) {
            String url = publicBaseUrl + "/" + plainToken;
            String subject = "Please sign document: " + doc.getFilename();
            String body = "You can sign the document using the following link: " + url + "\nThis link will expire at " + link.getExpiresAt();
            emailService.sendSimpleMessage(recipientEmail, subject, body);
        }

        return plainToken; // return the plaintext token so caller can show full URL
    }

    public Optional<PublicSignatureLink> findByPlainToken(String token) {
        String h = hashToken(token);
        return linkRepository.findByTokenHash(h);
    }

    @Transactional
    public boolean consumeToken(String token, String requestIp) {
        Optional<PublicSignatureLink> optional = findByPlainToken(token);
        if (optional.isEmpty()) return false;
        PublicSignatureLink link = optional.get();
        if (!"ACTIVE".equals(link.getStatus())) return false;
        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now())) {
            link.setStatus("EXPIRED");
            linkRepository.save(link);
            return false;
        }

        link.setAccessCount(link.getAccessCount() + 1);
        link.setLastAccessedAt(LocalDateTime.now());
        link.setUsedByIp(requestIp);
        if (link.getOneTime() != null && link.getOneTime()) {
            link.setUsedAt(LocalDateTime.now());
            link.setStatus("USED");
        }
        linkRepository.save(link);
        return true;
    }

    @Transactional
    public void revokeLink(Long id) {
        linkRepository.revokeById(id);
    }
}

