-- ========================================
-- 图灵学社 完整数据库建表脚本
-- MySQL 8.0+
-- ========================================

CREATE DATABASE IF NOT EXISTS turing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE turing;

-- ════════════════════════════════════
-- 1. 用户表（已有，添加索引）
-- ════════════════════════════════════
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS idx_username (username);
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_role (role);

-- ════════════════════════════════════
-- 2. 博客文章
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS blog_posts (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(200) NOT NULL,
  slug        VARCHAR(200) UNIQUE,
  content     TEXT NOT NULL,
  description VARCHAR(500),
  tags        VARCHAR(500),
  author_id   BIGINT,
  author_name VARCHAR(50),
  cover_emoji VARCHAR(10) DEFAULT '📝',
  status      ENUM('published','pending','rejected') DEFAULT 'pending',
  view_count  INT DEFAULT 0,
  created_at  DATETIME DEFAULT NOW(),
  updated_at  DATETIME DEFAULT NOW() ON UPDATE NOW(),
  INDEX idx_slug (slug),
  INDEX idx_status (status),
  INDEX idx_created (created_at),
  FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 3. 博客评论
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS blog_comments (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id     BIGINT NOT NULL,
  user_id     BIGINT,
  nickname    VARCHAR(50),
  content     TEXT NOT NULL,
  created_at  DATETIME DEFAULT NOW(),
  INDEX idx_post (post_id),
  FOREIGN KEY (post_id) REFERENCES blog_posts(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 4. 活动
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS events (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(200) NOT NULL,
  description TEXT,
  event_date  DATE NOT NULL,
  event_time  VARCHAR(50),
  location    VARCHAR(200),
  status      ENUM('open','soon','past') DEFAULT 'soon',
  cover_emoji VARCHAR(10) DEFAULT '📅',
  created_at  DATETIME DEFAULT NOW(),
  INDEX idx_status (status),
  INDEX idx_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 5. 活动报名
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS event_registrations (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id   BIGINT NOT NULL,
  user_id    BIGINT,
  name       VARCHAR(50) NOT NULL,
  phone      VARCHAR(20),
  email      VARCHAR(100),
  created_at DATETIME DEFAULT NOW(),
  INDEX idx_event (event_id),
  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 6. FAQ（官方维护）
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS faqs (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  question   VARCHAR(500) NOT NULL,
  answer     TEXT NOT NULL,
  author     VARCHAR(50),
  sort_order INT DEFAULT 0,
  created_at DATETIME DEFAULT NOW(),
  INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 7. 用户提问
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS user_questions (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT,
  nickname   VARCHAR(50),
  title      VARCHAR(200),
  content    TEXT,
  contact    VARCHAR(100),
  status     ENUM('pending','replied') DEFAULT 'pending',
  created_at DATETIME DEFAULT NOW(),
  INDEX idx_status (status),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 8. 提问回复
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS question_replies (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  question_id BIGINT NOT NULL,
  user_id     BIGINT,
  nickname    VARCHAR(50),
  content     TEXT NOT NULL,
  created_at  DATETIME DEFAULT NOW(),
  INDEX idx_question (question_id),
  FOREIGN KEY (question_id) REFERENCES user_questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 9. 资源
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS resources (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  title        VARCHAR(200) NOT NULL,
  description  TEXT,
  category     VARCHAR(50),
  file_type    VARCHAR(20),
  file_size    VARCHAR(20),
  link         VARCHAR(500),
  extract_code VARCHAR(20),
  uploader_id  BIGINT,
  like_count   INT DEFAULT 0,
  status       ENUM('published','pending') DEFAULT 'published',
  created_at   DATETIME DEFAULT NOW(),
  INDEX idx_category (category),
  INDEX idx_status (status),
  FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 10. 资源点赞
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS resource_likes (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  resource_id BIGINT NOT NULL,
  user_id     BIGINT NOT NULL,
  created_at  DATETIME DEFAULT NOW(),
  UNIQUE KEY uk_res_user (resource_id, user_id),
  INDEX idx_resource (resource_id),
  FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 11. 社团成员
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS members (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(50) NOT NULL,
  role         VARCHAR(50),
  department   VARCHAR(50),
  grade        VARCHAR(20),
  bio          TEXT,
  tags         VARCHAR(300),
  avatar_color VARCHAR(30),
  sort_order   INT DEFAULT 0,
  is_lead      BOOLEAN DEFAULT FALSE,
  created_at   DATETIME DEFAULT NOW(),
  INDEX idx_dept (department),
  INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 12. 指导老师
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS teachers (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(50) NOT NULL,
  title           VARCHAR(50),
  dept            VARCHAR(200),
  quote           TEXT,
  research        TEXT,
  tags            VARCHAR(300),
  avatar_gradient VARCHAR(100),
  sort_order      INT DEFAULT 0,
  created_at      DATETIME DEFAULT NOW(),
  INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 13. 项目展示
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS projects (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  description  TEXT,
  tech_tags    VARCHAR(300),
  github_link  VARCHAR(500),
  demo_link    VARCHAR(500),
  cover_emoji  VARCHAR(10) DEFAULT '🚀',
  member_count INT DEFAULT 1,
  star_count   INT DEFAULT 0,
  status       ENUM('active','completed','maintenance') DEFAULT 'active',
  created_at   DATETIME DEFAULT NOW(),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ════════════════════════════════════
-- 14. 网站配置
-- ════════════════════════════════════
CREATE TABLE IF NOT EXISTS site_config (
  config_key   VARCHAR(100) PRIMARY KEY,
  config_value TEXT,
  updated_at   DATETIME DEFAULT NOW() ON UPDATE NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
