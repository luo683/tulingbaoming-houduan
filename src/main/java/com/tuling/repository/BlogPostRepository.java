package com.tuling.repository;

import com.tuling.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Optional<BlogPost> findBySlug(String slug);

    List<BlogPost> findByStatus(String status);

    List<BlogPost> findByStatusOrderByCreatedAtDesc(String status);

    List<BlogPost> findAllByOrderByCreatedAtDesc();

    @Query("SELECT b FROM BlogPost b WHERE b.status = 'published' AND (b.title LIKE %:keyword% OR b.content LIKE %:keyword% OR b.tags LIKE %:keyword%)")
    List<BlogPost> searchByKeyword(@Param("keyword") String keyword);
}
