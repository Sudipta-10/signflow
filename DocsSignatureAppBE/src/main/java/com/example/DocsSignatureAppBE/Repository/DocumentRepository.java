package com.example.DocsSignatureAppBE.Repository;

import com.example.DocsSignatureAppBE.Entity.Document;
import com.example.DocsSignatureAppBE.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
     List<Document> findByUploadedBy(User user);

}
