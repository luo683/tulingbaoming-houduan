-- ==========================================
-- 图灵学社活动报名系统 - 数据库迁移脚本
-- 版本：V2
-- 创建时间：2024-01-10
-- ==========================================

-- 1. 创建活动报名记录表
CREATE TABLE IF NOT EXISTS event_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    event_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名（冗余）',
    student_id VARCHAR(30) COMMENT '学号',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    email VARCHAR(100) COMMENT '电子邮箱',
    status VARCHAR(20) NOT NULL DEFAULT 'registered' COMMENT '状态：registered-已报名/attended-已参加/cancelled-已取消',
    attended_at DATETIME COMMENT '签到时间',
    certificate_generated BOOLEAN DEFAULT FALSE COMMENT '是否已生成证明',
    certificate_url VARCHAR(255) COMMENT '证明文件URL',
    notes TEXT COMMENT '备注信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_event_user (event_id, user_id) COMMENT '同一用户不能重复报名同一活动',
    INDEX idx_event_id (event_id) COMMENT '活动ID索引',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    INDEX idx_status (status) COMMENT '状态索引',
    INDEX idx_created_at (created_at) COMMENT '报名时间索引',

    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名记录表';

-- 2. 扩展events表，添加报名相关字段
ALTER TABLE events
ADD COLUMN IF NOT EXISTS max_participants INT DEFAULT 0 COMMENT '最大参与人数，0表示无限制',
ADD COLUMN IF NOT EXISTS registration_deadline DATETIME COMMENT '报名截止时间',
ADD COLUMN IF NOT EXISTS allow_registration BOOLEAN DEFAULT TRUE COMMENT '是否允许报名';

-- 3. 更新现有活动数据（设置默认值）
UPDATE events SET allow_registration = TRUE WHERE allow_registration IS NULL;
UPDATE events SET max_participants = 0 WHERE max_participants IS NULL;

-- 4. 创建报名统计视图
CREATE OR REPLACE VIEW v_event_registration_stats AS
SELECT
    e.id AS event_id,
    e.title,
    e.event_date,
    e.max_participants,
    COUNT(r.id) AS total_registered,
    SUM(CASE WHEN r.status = 'attended' THEN 1 ELSE 0 END) AS total_attended,
    SUM(CASE WHEN r.status = 'cancelled' THEN 1 ELSE 0 END) AS total_cancelled,
    CASE
        WHEN e.max_participants > 0 AND COUNT(r.id) >= e.max_participants THEN TRUE
        ELSE FALSE
    END AS is_full
FROM events e
LEFT JOIN event_registrations r ON e.id = r.event_id AND r.status != 'cancelled'
GROUP BY e.id, e.title, e.event_date, e.max_participants;

-- 5. 插入测试数据（可选，仅开发环境）
-- INSERT INTO event_registrations
--     (event_id, user_id, username, student_id, real_name, phone, email, status, created_at)
-- VALUES
--     (1, 2, 'testuser1', '2021001', '张三', '13800138001', 'zhangsan@example.com', 'registered', NOW()),
--     (1, 3, 'testuser2', '2021002', '李四', '13800138002', 'lisi@example.com', 'attended', NOW());
