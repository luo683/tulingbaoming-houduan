package com.tuling.controller;

import com.tuling.dto.RegistrationRequest;
import com.tuling.dto.RegistrationStatsDTO;
import com.tuling.entity.EventRegistration;
import com.tuling.service.EventRegistrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 活动报名 REST Controller
 */
@RestController
@RequestMapping("/api")
public class EventRegistrationController {

    private static final Logger log = LoggerFactory.getLogger(EventRegistrationController.class);

    @Autowired
    private EventRegistrationService registrationService;

    // ── 公共端点 ──

    /**
     * 获取活动报名统计
     */
    @GetMapping("/events/{eventId}/registrations/stats")
    public ResponseEntity<Map<String, Object>> getRegistrationStats(@PathVariable Long eventId) {
        log.info("GET /api/events/{}/registrations/stats", eventId);
        try {
            RegistrationStatsDTO stats = registrationService.getRegistrationStats(eventId);
            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("活动不存在: eventId={}", eventId);
            Map<String, Object> response = new HashMap<>();
            response.put("ok", false);
            response.put("error", "活动不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 用户报名活动
     *
     * 需要从请求头或token中获取当前用户ID
     * 这里简化处理，从请求参数获取userId
     */
    @PostMapping("/events/{eventId}/registrations")
    public ResponseEntity<Map<String, Object>> registerForEvent(
            @PathVariable Long eventId,
            @RequestParam Long userId,
            @RequestBody RegistrationRequest request) {

        log.info("POST /api/events/{}/registrations, userId={}", eventId, userId);

        Map<String, Object> response = new HashMap<>();
        try {
            EventRegistration registration = registrationService.registerForEvent(eventId, userId, request);
            response.put("ok", true);
            response.put("data", registration);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("报名失败: {}", e.getMessage());
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalStateException e) {
            log.warn("报名失败: {}", e.getMessage());
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * 取消报名
     */
    @DeleteMapping("/events/{eventId}/registrations")
    public ResponseEntity<Map<String, Object>> cancelRegistration(
            @PathVariable Long eventId,
            @RequestParam Long userId) {

        log.info("DELETE /api/events/{}/registrations, userId={}", eventId, userId);

        Map<String, Object> response = new HashMap<>();
        try {
            registrationService.cancelRegistration(eventId, userId);
            response.put("ok", true);
            response.put("message", "取消报名成功");
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("取消失败: {}", e.getMessage());
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalStateException e) {
            log.warn("取消失败: {}", e.getMessage());
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * 获取我的报名记录
     */
    @GetMapping("/registrations/my")
    public ResponseEntity<Map<String, Object>> getMyRegistrations(@RequestParam Long userId) {
        log.info("GET /api/registrations/my, userId={}", userId);

        List<EventRegistration> registrations = registrationService.getUserRegistrations(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("data", registrations);
        return ResponseEntity.ok(response);
    }

    /**
     * 检查是否已报名
     */
    @GetMapping("/events/{eventId}/registrations/check")
    public ResponseEntity<Map<String, Object>> checkRegistration(
            @PathVariable Long eventId,
            @RequestParam Long userId) {

        log.debug("GET /api/events/{}/registrations/check, userId={}", eventId, userId);

        boolean isRegistered = registrationService.isUserRegistered(eventId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("isRegistered", isRegistered);
        return ResponseEntity.ok(response);
    }

    // ── 管理端点 ──

    /**
     * 管理员：获取活动报名详情列表
     */
    @GetMapping("/events/{eventId}/registrations/admin")
    public ResponseEntity<Map<String, Object>> getEventRegistrations(@PathVariable Long eventId) {
        log.info("GET /api/events/{}/registrations/admin", eventId);

        List<EventRegistration> registrations = registrationService.getEventRegistrations(eventId);
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("data", registrations);
        return ResponseEntity.ok(response);
    }

    /**
     * 管理员：标记签到
     */
    @PutMapping("/registrations/{id}/attend")
    public ResponseEntity<Map<String, Object>> markAttendance(@PathVariable Long id) {
        log.info("PUT /api/registrations/{}/attend", id);

        Map<String, Object> response = new HashMap<>();
        try {
            EventRegistration registration = registrationService.markAttendance(id);
            response.put("ok", true);
            response.put("data", registration);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("签到失败: {}", e.getMessage());
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalStateException e) {
            log.warn("签到失败: {}", e.getMessage());
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * 管理员：生成证明
     */
    @PostMapping("/registrations/{id}/certificate")
    public ResponseEntity<Map<String, Object>> generateCertificate(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        log.info("POST /api/registrations/{}/certificate", id);

        String certificateUrl = payload.get("certificateUrl");

        Map<String, Object> response = new HashMap<>();
        try {
            EventRegistration registration = registrationService.generateCertificate(id, certificateUrl);
            response.put("ok", true);
            response.put("data", registration);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("生成证明失败: {}", e.getMessage());
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalStateException e) {
            log.warn("生成证明失败: {}", e.getMessage());
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
}
