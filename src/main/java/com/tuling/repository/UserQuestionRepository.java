package com.tuling.repository;

import com.tuling.entity.UserQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQuestionRepository extends JpaRepository<UserQuestion, Long> {

    List<UserQuestion> findByStatusOrderByCreatedAtDesc(String status);
}
