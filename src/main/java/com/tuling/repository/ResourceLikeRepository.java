package com.tuling.repository;

import com.tuling.entity.ResourceLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ResourceLikeRepository extends JpaRepository<ResourceLike, Long> {

    Optional<ResourceLike> findByResourceIdAndUserId(Long resourceId, Long userId);

    long countByResourceId(Long resourceId);

    @Transactional
    void deleteByResourceIdAndUserId(Long resourceId, Long userId);
}
