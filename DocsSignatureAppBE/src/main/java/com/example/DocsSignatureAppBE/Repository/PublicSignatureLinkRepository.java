package com.example.DocsSignatureAppBE.Repository;

import com.example.DocsSignatureAppBE.Entity.Document;
import com.example.DocsSignatureAppBE.Entity.PublicSignatureLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicSignatureLinkRepository extends JpaRepository<PublicSignatureLink, Long> {
    Optional<PublicSignatureLink> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE PublicSignatureLink p SET p.status = 'REVOKED' WHERE p.id = :id")
    int revokeById(@Param("id") Long id);
}

