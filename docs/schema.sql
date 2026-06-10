-- ============================================
-- 学生成绩管理系统 数据库建表脚本
-- MySQL 8.0+
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 如果手动执行，请先创建数据库：
-- CREATE DATABASE IF NOT EXISTS student_grade_system DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
-- USE student_grade_system;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    url VARCHAR(200) DEFAULT NULL COMMENT '请求URL',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '类型 1-菜单 2-按钮',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 班级表
DROP TABLE IF EXISTS class_info;
CREATE TABLE class_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    grade VARCHAR(20) NOT NULL COMMENT '年级',
    major VARCHAR(50) DEFAULT NULL COMMENT '专业',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_grade (grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 学生表
DROP TABLE IF EXISTS student;
CREATE TABLE student (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_no VARCHAR(30) NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT DEFAULT 1 COMMENT '性别 1-男 2-女',
    class_id BIGINT DEFAULT NULL COMMENT '班级ID',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 教师表
DROP TABLE IF EXISTS teacher;
CREATE TABLE teacher (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    teacher_no VARCHAR(30) NOT NULL COMMENT '工号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT DEFAULT 1 COMMENT '性别 1-男 2-女',
    title VARCHAR(30) DEFAULT NULL COMMENT '职称',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 课程表
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_no VARCHAR(30) NOT NULL COMMENT '课程编号',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit INT DEFAULT 0 COMMENT '学分',
    teacher_id BIGINT DEFAULT NULL COMMENT '授课教师ID',
    semester VARCHAR(30) DEFAULT NULL COMMENT '学期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_no (course_no),
    KEY idx_teacher_id (teacher_id),
    KEY idx_semester (semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 成绩表
DROP TABLE IF EXISTS score;
CREATE TABLE score (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '成绩',
    semester VARCHAR(30) DEFAULT NULL COMMENT '学期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course (student_id, course_id, semester),
    KEY idx_course_id (course_id),
    KEY idx_semester (semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 初始数据
INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限'),
('教师', 'ROLE_TEACHER', '教师角色'),
('学生', 'ROLE_STUDENT', '学生角色');

-- 密码 admin123 的 BCrypt 哈希
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$10$r2t2gN3TDO9Cks9IIoUSJ.Xi/PK7iTS62sE1r7xZUusQ03rk6lvr6', '管理员', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- ============================================
-- 真实测试数据
-- ============================================

-- 权限数据
INSERT INTO sys_permission (permission_name, permission_code, url, type, parent_id, sort_order) VALUES
('学生管理', 'student:manage', '/student', 1, 0, 1),
('教师管理', 'teacher:manage', '/teacher', 1, 0, 2),
('课程管理', 'course:manage', '/course', 1, 0, 3),
('成绩管理', 'score:manage', '/score', 1, 0, 4),
('成绩分析', 'score:analysis', '/analysis', 1, 0, 5),
('学生新增', 'student:add', NULL, 2, 1, 1),
('学生编辑', 'student:edit', NULL, 2, 1, 2),
('学生删除', 'student:delete', NULL, 2, 1, 3),
('成绩导入', 'score:import', NULL, 2, 4, 1),
('成绩导出', 'score:export', NULL, 2, 4, 2);

-- 管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10);

-- 教师权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 3), (2, 4), (2, 5), (2, 9), (2, 10);

-- 班级数据
INSERT INTO class_info (class_name, grade, major) VALUES
('计算机2201班', '2022', '计算机科学与技术'),
('计算机2202班', '2022', '计算机科学与技术'),
('软件2201班', '2022', '软件工程'),
('软件2202班', '2022', '软件工程'),
('信安2201班', '2022', '信息安全'),
('计算机2301班', '2023', '计算机科学与技术'),
('计算机2302班', '2023', '计算机科学与技术'),
('软件2301班', '2023', '软件工程'),
('数据2301班', '2023', '数据科学与大数据技术'),
('人工智能2301班', '2023', '人工智能');

-- 教师数据
INSERT INTO teacher (teacher_no, name, gender, title, phone) VALUES
('T20200101', '张建国', 1, '教授', '13800001001'),
('T20200102', '李秀英', 2, '副教授', '13800001002'),
('T20200103', '王志强', 1, '讲师', '13800001003'),
('T20200104', '陈丽华', 2, '副教授', '13800001004'),
('T20200105', '刘德明', 1, '教授', '13800001005'),
('T20200106', '赵雅芳', 2, '讲师', '13800001006'),
('T20200107', '孙伟', 1, '副教授', '13800001007'),
('T20200108', '周敏', 2, '讲师', '13800001008');

-- 学生数据
INSERT INTO student (student_no, name, gender, class_id, phone) VALUES
('S2022010101', '王明', 1, 1, '15900001001'),
('S2022010102', '李芳', 2, 1, '15900001002'),
('S2022010103', '张伟', 1, 1, '15900001003'),
('S2022010104', '刘洋', 1, 1, '15900001004'),
('S2022010105', '陈静', 2, 1, '15900001005'),
('S2022010201', '赵磊', 1, 2, '15900001006'),
('S2022010202', '孙丽', 2, 2, '15900001007'),
('S2022010203', '周杰', 1, 2, '15900001008'),
('S2022010204', '吴敏', 2, 2, '15900001009'),
('S2022010205', '郑浩', 1, 2, '15900001010'),
('S2022020101', '黄婷', 2, 3, '15900001011'),
('S2022020102', '林涛', 1, 3, '15900001012'),
('S2022020103', '何雪', 2, 3, '15900001013'),
('S2022020104', '马超', 1, 3, '15900001014'),
('S2022020105', '罗琳', 2, 3, '15900001015'),
('S2022020201', '谢峰', 1, 4, '15900001016'),
('S2022020202', '韩梅', 2, 4, '15900001017'),
('S2022020203', '唐亮', 1, 4, '15900001018'),
('S2022020204', '冯雨', 2, 4, '15900001019'),
('S2022020205', '曹鹏', 1, 4, '15900001020'),
('S2023010101', '许阳', 1, 6, '15900001021'),
('S2023010102', '邓颖', 2, 6, '15900001022'),
('S2023010103', '萧然', 1, 6, '15900001023'),
('S2023010104', '田甜', 2, 6, '15900001024'),
('S2023010105', '方正', 1, 6, '15900001025'),
('S2023020101', '石磊', 1, 8, '15900001026'),
('S2023020102', '姚瑶', 2, 8, '15900001027'),
('S2023020103', '段宇', 1, 8, '15900001028'),
('S2023020104', '贺兰', 2, 8, '15900001029'),
('S2023020105', '龚翔', 1, 8, '15900001030');

-- 课程数据
INSERT INTO course (course_no, course_name, credit, teacher_id, semester) VALUES
('CS101', '高等数学', 5, 1, '2024-2025-1'),
('CS102', '线性代数', 4, 2, '2024-2025-1'),
('CS103', '大学英语', 3, 3, '2024-2025-1'),
('CS201', '数据结构与算法', 4, 1, '2024-2025-1'),
('CS202', '计算机组成原理', 4, 4, '2024-2025-1'),
('CS203', '操作系统', 4, 5, '2024-2025-2'),
('CS204', '计算机网络', 3, 6, '2024-2025-2'),
('CS301', '数据库原理', 4, 2, '2024-2025-2'),
('CS302', '软件工程', 3, 7, '2024-2025-2'),
('CS303', 'Java程序设计', 4, 8, '2024-2025-1'),
('CS304', 'Python编程基础', 3, 3, '2024-2025-1'),
('CS305', '人工智能导论', 3, 5, '2024-2025-2');

-- 成绩统计分析表（Quartz定时任务生成）
DROP TABLE IF EXISTS score_statistics;
CREATE TABLE score_statistics (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    stat_type VARCHAR(20) NOT NULL COMMENT '统计类型 DAILY/WEEKLY',
    student_count INT NOT NULL DEFAULT 0 COMMENT '参与人数',
    avg_score DECIMAL(5,2) DEFAULT NULL COMMENT '平均分',
    max_score DECIMAL(5,2) DEFAULT NULL COMMENT '最高分',
    min_score DECIMAL(5,2) DEFAULT NULL COMMENT '最低分',
    pass_rate DECIMAL(5,2) DEFAULT NULL COMMENT '及格率(%)',
    excellent_rate DECIMAL(5,2) DEFAULT NULL COMMENT '优秀率(%)',
    stat_time DATETIME NOT NULL COMMENT '统计时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_stat_type (stat_type),
    KEY idx_stat_time (stat_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩统计分析表';

-- 成绩数据（2024-2025-1学期）
INSERT INTO score (student_id, course_id, score, semester) VALUES
-- 王明的成绩
(1, 1, 92.5, '2024-2025-1'), (1, 2, 88.0, '2024-2025-1'), (1, 3, 76.5, '2024-2025-1'),
(1, 4, 95.0, '2024-2025-1'), (1, 10, 91.0, '2024-2025-1'),
-- 李芳的成绩
(2, 1, 85.0, '2024-2025-1'), (2, 2, 90.5, '2024-2025-1'), (2, 3, 88.0, '2024-2025-1'),
(2, 4, 82.0, '2024-2025-1'), (2, 10, 87.5, '2024-2025-1'),
-- 张伟的成绩
(3, 1, 78.0, '2024-2025-1'), (3, 2, 72.5, '2024-2025-1'), (3, 3, 81.0, '2024-2025-1'),
(3, 4, 75.0, '2024-2025-1'), (3, 10, 80.0, '2024-2025-1'),
-- 刘洋的成绩
(4, 1, 88.5, '2024-2025-1'), (4, 2, 91.0, '2024-2025-1'), (4, 3, 70.0, '2024-2025-1'),
(4, 4, 86.0, '2024-2025-1'), (4, 10, 89.0, '2024-2025-1'),
-- 陈静的成绩
(5, 1, 95.0, '2024-2025-1'), (5, 2, 93.5, '2024-2025-1'), (5, 3, 92.0, '2024-2025-1'),
(5, 4, 97.0, '2024-2025-1'), (5, 10, 94.5, '2024-2025-1'),
-- 赵磊的成绩
(6, 1, 65.0, '2024-2025-1'), (6, 2, 70.0, '2024-2025-1'), (6, 3, 73.5, '2024-2025-1'),
(6, 4, 68.0, '2024-2025-1'), (6, 10, 71.0, '2024-2025-1'),
-- 孙丽的成绩
(7, 1, 82.0, '2024-2025-1'), (7, 2, 85.5, '2024-2025-1'), (7, 3, 90.0, '2024-2025-1'),
(7, 4, 79.0, '2024-2025-1'), (7, 10, 83.0, '2024-2025-1'),
-- 周杰的成绩
(8, 1, 71.0, '2024-2025-1'), (8, 2, 68.5, '2024-2025-1'), (8, 3, 75.0, '2024-2025-1'),
(8, 4, 73.0, '2024-2025-1'), (8, 10, 70.5, '2024-2025-1'),
-- 吴敏的成绩
(9, 1, 89.0, '2024-2025-1'), (9, 2, 86.0, '2024-2025-1'), (9, 3, 84.5, '2024-2025-1'),
(9, 4, 90.0, '2024-2025-1'), (9, 10, 88.0, '2024-2025-1'),
-- 郑浩的成绩
(10, 1, 76.5, '2024-2025-1'), (10, 2, 74.0, '2024-2025-1'), (10, 3, 79.0, '2024-2025-1'),
(10, 4, 77.5, '2024-2025-1'), (10, 10, 75.0, '2024-2025-1'),
-- 黄婷（软件2201）
(11, 1, 91.0, '2024-2025-1'), (11, 2, 87.0, '2024-2025-1'), (11, 3, 85.5, '2024-2025-1'),
(11, 4, 93.0, '2024-2025-1'), (11, 11, 90.0, '2024-2025-1'),
-- 林涛
(12, 1, 73.0, '2024-2025-1'), (12, 2, 78.5, '2024-2025-1'), (12, 3, 71.0, '2024-2025-1'),
(12, 4, 76.0, '2024-2025-1'), (12, 11, 74.5, '2024-2025-1'),
-- 何雪
(13, 1, 86.5, '2024-2025-1'), (13, 2, 89.0, '2024-2025-1'), (13, 3, 91.5, '2024-2025-1'),
(13, 4, 84.0, '2024-2025-1'), (13, 11, 88.0, '2024-2025-1'),
-- 马超
(14, 1, 60.0, '2024-2025-1'), (14, 2, 63.5, '2024-2025-1'), (14, 3, 58.0, '2024-2025-1'),
(14, 4, 62.0, '2024-2025-1'), (14, 11, 65.0, '2024-2025-1'),
-- 罗琳
(15, 1, 94.0, '2024-2025-1'), (15, 2, 96.0, '2024-2025-1'), (15, 3, 93.0, '2024-2025-1'),
(15, 4, 98.0, '2024-2025-1'), (15, 11, 95.5, '2024-2025-1');

-- 成绩预警表
DROP TABLE IF EXISTS score_alert;
CREATE TABLE score_alert (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    prev_score DECIMAL(5,2) NOT NULL COMMENT '上次考试成绩',
    curr_score DECIMAL(5,2) NOT NULL COMMENT '本次考试成绩',
    drop_score DECIMAL(5,2) NOT NULL COMMENT '下降分数',
    alert_level VARCHAR(20) NOT NULL COMMENT '预警等级: NORMAL-一般, WARNING-警告, DANGER-危险',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-未处理 1-已处理',
    semester VARCHAR(30) DEFAULT NULL COMMENT '学期',
    handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_student_id (student_id),
    KEY idx_course_id (course_id),
    KEY idx_status (status),
    KEY idx_alert_level (alert_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩预警表';

-- 成绩数据（2024-2025-2学期）
INSERT INTO score (student_id, course_id, score, semester) VALUES
(1, 6, 90.0, '2024-2025-2'), (1, 7, 85.5, '2024-2025-2'), (1, 8, 93.0, '2024-2025-2'),
(2, 6, 83.0, '2024-2025-2'), (2, 7, 87.0, '2024-2025-2'), (2, 8, 86.5, '2024-2025-2'),
(3, 6, 72.0, '2024-2025-2'), (3, 7, 76.5, '2024-2025-2'), (3, 8, 74.0, '2024-2025-2'),
(4, 6, 85.0, '2024-2025-2'), (4, 7, 82.0, '2024-2025-2'), (4, 8, 88.5, '2024-2025-2'),
(5, 6, 96.5, '2024-2025-2'), (5, 7, 94.0, '2024-2025-2'), (5, 8, 97.0, '2024-2025-2'),
(6, 6, 67.0, '2024-2025-2'), (6, 7, 71.5, '2024-2025-2'), (6, 8, 69.0, '2024-2025-2'),
(7, 6, 80.0, '2024-2025-2'), (7, 7, 84.0, '2024-2025-2'), (7, 8, 81.5, '2024-2025-2'),
(8, 6, 69.0, '2024-2025-2'), (8, 7, 73.0, '2024-2025-2'), (8, 8, 71.0, '2024-2025-2'),
(11, 6, 89.0, '2024-2025-2'), (11, 7, 86.0, '2024-2025-2'), (11, 9, 92.0, '2024-2025-2'),
(12, 6, 75.0, '2024-2025-2'), (12, 7, 72.0, '2024-2025-2'), (12, 9, 77.5, '2024-2025-2'),
(13, 6, 88.0, '2024-2025-2'), (13, 7, 90.5, '2024-2025-2'), (13, 9, 87.0, '2024-2025-2');
