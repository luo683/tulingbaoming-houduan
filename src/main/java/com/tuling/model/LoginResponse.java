package com.tuling.model;

import java.time.LocalDateTime;

/**
 * 登录成功响应体（含用户信息 + 会话 token）
 */
public class LoginResponse {

    private boolean ok;
    private String error;
    private String username;
    private String role;
    private String avatar;
    private String token;
    private LocalDateTime createdAt;

    public LoginResponse() {}

    public static LoginResponse success(String username, String role, String avatar, String token) {
        LoginResponse r = new LoginResponse();
        r.ok = true;
        r.username = username;
        r.role = role;
        r.avatar = avatar;
        r.token = token;
        return r;
    }

    public static LoginResponse fail(String error) {
        LoginResponse r = new LoginResponse();
        r.ok = false;
        r.error = error;
        return r;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
