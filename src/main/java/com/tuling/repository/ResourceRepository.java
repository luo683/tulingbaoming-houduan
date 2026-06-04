package com.tuling.repository;

import com.tuling.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByStatusOrderByCreatedAtDesc(String status);

    List<Resource> findByCategoryOrderByCreatedAtDesc(String category);
}
