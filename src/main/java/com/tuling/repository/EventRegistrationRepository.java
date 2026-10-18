package com.tuling.repository;

import com.tuling.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    /**
     * 查找指定活动的所有报名记录
     */
    List<EventRegistration> findByEventIdOrderByCreatedAtDesc(Long eventId);

    /**
     * 查找指定用户的所有报名记录
     */
    List<EventRegistration> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 查找指定活动和用户的报名记录
     */
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * 统计活动报名人数（不包括已取消）
     */
    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.eventId = :eventId AND r.status != 'cancelled'")
    long countByEventIdExcludingCancelled(@Param("eventId") Long eventId);

    /**
     * 统计指定状态的报名人数
     */
    long countByEventIdAndStatus(Long eventId, String status);

    /**
     * 检查用户是否已报名（且未取消）
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM EventRegistration r " +
           "WHERE r.eventId = :eventId AND r.userId = :userId AND r.status != 'cancelled'")
    boolean existsActiveRegistration(@Param("eventId") Long eventId, @Param("userId") Long userId);

    /**
     * 获取活动的有效报名列表（未取消）
     */
    @Query("SELECT r FROM EventRegistration r " +
           "WHERE r.eventId = :eventId AND r.status != 'cancelled' " +
           "ORDER BY r.createdAt DESC")
    List<EventRegistration> findActiveRegistrationsByEventId(@Param("eventId") Long eventId);
}
