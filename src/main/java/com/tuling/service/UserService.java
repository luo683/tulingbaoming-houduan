package com.tuling.service;

import com.tuling.model.LoginResponse;
import com.tuling.model.User;
import com.tuling.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户认证服务
 * 注册 / 登录 / 修改密码 / 锁定 / 会话管理
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 注册新用户（默认为普通用户，管理员的 admin 账号由数据库初始化脚本创建）
     */
    @Transactional
    public LoginResponse register(String username, String password,
                                   String college, String studentId,
                                   String major, String grade) {
        // 校验用户名
        if (username == null || username.trim().length() < 2) {
            return LoginResponse.fail("用户名至少 2 个字符");
        }
        if (username.trim().length() > 50) {
            return LoginResponse.fail("用户名不能超过 50 个字符");
        }
        if (!username.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
            return LoginResponse.fail("用户名只能包含中英文、数字和下划线");
        }

        // 校验密码
        if (password == null || password.length() < 4) {
            return LoginResponse.fail("密码至少 4 位");
        }

        // 普通用户必须填写学院、学号、专业、年级
        if (college == null || college.trim().isEmpty()) {
            return LoginResponse.fail("请填写学院");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            return LoginResponse.fail("请填写学号");
        }
        if (major == null || major.trim().isEmpty()) {
            return LoginResponse.fail("请填写专业");
        }
        if (grade == null || grade.trim().isEmpty()) {
            return LoginResponse.fail("请填写年级");
        }

        // 检查是否已存在
        if (userRepository.existsByUsername(username.trim())) {
            return LoginResponse.fail("该用户名已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("user");
        user.setAvatar("🧑‍🎓");
        user.setCreatedAt(LocalDateTime.now());
        user.setCollege(college.trim());
        user.setStudentId(studentId.trim());
        user.setMajor(major.trim());
        user.setGrade(grade.trim());

        userRepository.save(user);
        log.info("新用户注册: {}", username.trim());

        String token = generateToken(username.trim());
        return LoginResponse.success(user.getId(), username.trim(), "user", "🧑‍🎓", token);
    }

    /**
     * 登录认证
     */
    @Transactional
    public LoginResponse login(String username, String password) {
        if (username == null || password == null) {
            return LoginResponse.fail("用户名和密码不能为空");
        }

        Optional<User> opt = userRepository.findByUsername(username.trim());
        if (!opt.isPresent()) {
            return LoginResponse.fail("账号不存在");
        }

        User user = opt.get();

        // 检查是否锁定
        if (user.isLocked()) {
            return LoginResponse.fail("账号已被锁定，请联系管理员");
        }

        // 验证密码（BCrypt）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLocked(true);
                log.warn("用户 {} 登录失败 {} 次，已锁定", username, user.getFailedAttempts());
                userRepository.save(user);
                return LoginResponse.fail("密码错误次数过多，账号已锁定。请联系管理员解锁。");
            }
            userRepository.save(user);
            int remaining = MAX_FAILED_ATTEMPTS - user.getFailedAttempts();
            return LoginResponse.fail("密码错误，还剩 " + remaining + " 次尝试机会");
        }

        // 登录成功 → 重置失败计数
        user.setFailedAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = generateToken(username.trim());
        log.info("用户 {} 登录成功", username.trim());
        return LoginResponse.success(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getAvatar(),
                token
        );
    }

    /**
     * 修改密码
     */
    @Transactional
    public LoginResponse changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (!opt.isPresent()) {
            return LoginResponse.fail("用户不存在");
        }

        User user = opt.get();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return LoginResponse.fail("原密码错误");
        }

        if (newPassword == null || newPassword.length() < 4) {
            return LoginResponse.fail("新密码至少 4 位");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedAttempts(0);
        user.setLocked(false);
        userRepository.save(user);

        log.info("用户 {} 修改密码成功", username);
        return LoginResponse.success(user.getId(), username, user.getRole(), user.getAvatar(), "");
    }

    /**
     * 生成简单会话 token（UUID）
     */
    private String generateToken(String username) {
        return UUID.randomUUID().toString().replace("-", "")
                + "." + username.hashCode();
    }
}
