package com.tuling.controller;

import com.tuling.model.LoginRequest;
import com.tuling.model.LoginResponse;
import com.tuling.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * 用户认证控制器
 * 注册 / 登录 / 修改密码
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    /**
     * 用户注册（默认普通用户角色）
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody LoginRequest request) {
        log.info("收到注册请求: {}", request.getUsername());
        LoginResponse response = userService.register(
                request.getUsername(), request.getPassword(),
                request.getCollege(), request.getStudentId(),
                request.getMajor(), request.getGrade()
        );
        if (response.isOk()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("收到登录请求: {}", request.getUsername());
        LoginResponse response = userService.login(request.getUsername(), request.getPassword());
        if (response.isOk()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(response);
    }

    /**
     * 修改密码
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<LoginResponse> changePassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        LoginResponse response = userService.changePassword(username, oldPassword, newPassword);
        if (response.isOk()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }
}
