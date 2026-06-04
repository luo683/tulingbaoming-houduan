package com.tuling.repository;

import com.tuling.entity.QuestionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionReplyRepository extends JpaRepository<QuestionReply, Long> {

    List<QuestionReply> findByQuestionIdOrderByCreatedAtAsc(Long questionId);
}
