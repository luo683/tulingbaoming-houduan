package com.tuling.service;

import com.tuling.dto.RegistrationRequest;
import com.tuling.dto.RegistrationStatsDTO;
import com.tuling.entity.Event;
import com.tuling.entity.EventRegistration;
import com.tuling.model.User;
import com.tuling.repository.EventRegistrationRepository;
import com.tuling.repository.EventRepository;
import com.tuling.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EventRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(EventRegistrationService.class);

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户报名活动
     */
    @Transactional
    public EventRegistration registerForEvent(Long eventId, Long userId, RegistrationRequest request) {
        log.info("用户 {} 尝试报名活动 {}", userId, eventId);

        // 1. 验证活动存在
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("活动不存在"));

        // 2. 验证用户存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));

        // 3. 检查活动是否允许报名
        if (!Boolean.TRUE.equals(event.getAllowRegistration())) {
            throw new IllegalStateException("该活动不允许报名");
        }

        // 4. 检查报名截止时间
        if (event.getRegistrationDeadline() != null
            && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new IllegalStateException("报名已截止");
        }

        // 5. 检查是否已报名
        if (registrationRepository.existsActiveRegistration(eventId, userId)) {
            throw new IllegalStateException("您已经报名过该活动");
        }

        // 6. 检查人数限制
        if (event.getMaxParticipants() != null && event.getMaxParticipants() > 0) {
            long currentCount = registrationRepository.countByEventIdExcludingCancelled(eventId);
            if (currentCount >= event.getMaxParticipants()) {
                throw new IllegalStateException("报名人数已满");
            }
        }

        // 7. 创建报名记录
        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setUserId(userId);
        registration.setUsername(user.getUsername());
        registration.setStudentId(user.getStudentId());
        registration.setRealName(request.getRealName());
        registration.setPhone(request.getPhone());
        registration.setEmail(request.getEmail());
        registration.setNotes(request.getNotes());
        registration.setStatus("registered");

        EventRegistration saved = registrationRepository.save(registration);
        log.info("报名成功：registrationId={}", saved.getId());

        return saved;
    }

    /**
     * 取消报名
     */
    @Transactional
    public void cancelRegistration(Long eventId, Long userId) {
        log.info("用户 {} 尝试取消报名活动 {}", userId, eventId);

        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NoSuchElementException("未找到报名记录"));

        if ("cancelled".equals(registration.getStatus())) {
            throw new IllegalStateException("报名已经取消过了");
        }

        if ("attended".equals(registration.getStatus())) {
            throw new IllegalStateException("活动已参加，无法取消报名");
        }

        registration.setStatus("cancelled");
        registrationRepository.save(registration);

        log.info("取消报名成功：registrationId={}", registration.getId());
    }

    /**
     * 获取活动的所有报名记录（管理员）
     */
    public List<EventRegistration> getEventRegistrations(Long eventId) {
        log.debug("获取活动 {} 的报名记录", eventId);
        return registrationRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    /**
     * 获取用户的所有报名记录
     */
    public List<EventRegistration> getUserRegistrations(Long userId) {
        log.debug("获取用户 {} 的报名记录", userId);
        return registrationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取活动报名统计
     */
    public RegistrationStatsDTO getRegistrationStats(Long eventId) {
        log.debug("获取活动 {} 的报名统计", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("活动不存在"));

        long totalRegistered = registrationRepository.countByEventIdExcludingCancelled(eventId);
        long totalAttended = registrationRepository.countByEventIdAndStatus(eventId, "attended");
        long totalCancelled = registrationRepository.countByEventIdAndStatus(eventId, "cancelled");

        return new RegistrationStatsDTO(
            eventId,
            totalRegistered,
            totalAttended,
            totalCancelled,
            event.getMaxParticipants()
        );
    }

    /**
     * 标记签到（管理员）
     */
    @Transactional
    public EventRegistration markAttendance(Long registrationId) {
        log.info("标记签到：registrationId={}", registrationId);

        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new NoSuchElementException("报名记录不存在"));

        if ("cancelled".equals(registration.getStatus())) {
            throw new IllegalStateException("已取消的报名无法签到");
        }

        registration.setStatus("attended");
        registration.setAttendedAt(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    /**
     * 生成证明（管理员）
     */
    @Transactional
    public EventRegistration generateCertificate(Long registrationId, String certificateUrl) {
        log.info("生成证明：registrationId={}, url={}", registrationId, certificateUrl);

        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new NoSuchElementException("报名记录不存在"));

        if (!"attended".equals(registration.getStatus())) {
            throw new IllegalStateException("只有已参加的活动才能生成证明");
        }

        registration.setCertificateGenerated(true);
        registration.setCertificateUrl(certificateUrl);

        return registrationRepository.save(registration);
    }

    /**
     * 检查用户是否已报名
     */
    public boolean isUserRegistered(Long eventId, Long userId) {
        return registrationRepository.existsActiveRegistration(eventId, userId);
    }
}
