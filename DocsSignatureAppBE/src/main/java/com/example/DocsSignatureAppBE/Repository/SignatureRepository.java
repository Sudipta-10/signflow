package com.example.DocsSignatureAppBE.Repository;

import com.example.DocsSignatureAppBE.Entity.Signature;
import com.example.DocsSignatureAppBE.Entity.SignatureStatus;
import com.example.DocsSignatureAppBE.Entity.Document;
import com.example.DocsSignatureAppBE.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Long> {

    /**
     * Find all signatures for a specific document
     */
    List<Signature> findByDocument(Document document);

    /**
     * Find all signatures by a specific signer
     */
    List<Signature> findBySigner(User signer);

    /**
     * Find all pending signatures for a document
     */
    List<Signature> findByDocumentAndStatus(Document document, SignatureStatus status);

    /**
     * Find all pending signatures for a signer
     */
    List<Signature> findBySignerAndStatus(User signer, SignatureStatus status);

    /**
     * Find signature by document and signer
     */
    Optional<Signature> findByDocumentAndSigner(Document document, User signer);

    /**
     * Count pending signatures for a document
     */
    long countByDocumentAndStatus(Document document, SignatureStatus status);

    /**
     * Count signed signatures for a document
     */
    long countByDocumentAndSignedDateIsNotNull(Document document);

    /**
     * Find all pending signatures for a signer
     */
    List<Signature> findBySignerAndStatusOrderByCreatedDateDesc(User signer, SignatureStatus status);

    /**
     * Find all signatures for a document ordered by page and coordinates
     */
    @Query("SELECT s FROM Signature s WHERE s.document = :document ORDER BY s.pageNumber ASC, s.xCoordinate ASC")
    List<Signature> findByDocumentOrderByPageNumberAscXCoordinateAsc(@Param("document") Document document);
}

