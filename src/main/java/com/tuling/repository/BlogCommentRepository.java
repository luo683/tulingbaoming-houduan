package com.tuling.repository;

import com.tuling.entity.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    List<BlogComment> findByPostIdOrderByCreatedAtDesc(Long postId);
}
