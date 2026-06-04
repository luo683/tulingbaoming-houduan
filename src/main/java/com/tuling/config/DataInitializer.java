package com.tuling.config;

import com.tuling.model.User;
import com.tuling.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化 — 首次启动时插入预设管理员和 demo 用户
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // admin 账号
        createIfNotExists("admin", "tuling2024", "admin", "🛡️");

        // demo 用户
        createIfNotExists("demo", "demo", "user", "👤");

        // 普通测试用户
        createIfNotExists("user", "tuling123", "user", "🧑‍🎓");

        log.info("预置账号初始化完成：admin / demo / user");
    }

    private void createIfNotExists(String username, String password, String role, String avatar) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setAvatar(avatar);
            userRepository.save(user);
        }
    }
}
