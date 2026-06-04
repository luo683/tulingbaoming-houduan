package com.tuling.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体（管理员 / 普通用户）
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名（唯一） */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt 加密后的密码 */
    @Column(nullable = false, length = 100)
    private String password;

    /** 角色：admin / user */
    @Column(nullable = false, length = 10)
    private String role;

    /** 头像 emoji */
    @Column(length = 10)
    private String avatar;

    /** 注册时间 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 最后登录时间 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 连续登录失败次数 */
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    /** 账号是否锁定 */
    @Column(nullable = false)
    private boolean locked = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (role == null) role = "user";
        if (avatar == null) avatar = "🧑‍🎓";
    }

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.avatar = "admin".equals(role) ? "🛡️" : "🧑‍🎓";
    }

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
}
