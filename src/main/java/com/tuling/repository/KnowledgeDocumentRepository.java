package com.tuling.repository;

import com.tuling.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findAllByOrderByUpdatedAtDesc();

    List<KnowledgeDocument> findByCategoryOrderByUpdatedAtDesc(String category);

    @Query("SELECT k FROM KnowledgeDocument k WHERE k.title LIKE %:keyword% OR k.content LIKE %:keyword% OR k.tags LIKE %:keyword%")
    List<KnowledgeDocument> searchByKeyword(@Param("keyword") String keyword);
}
