-- ============================================================
-- 教育培训机构教务收费管理系统(EduFeeMS) - 数据库初始化脚本
-- 数据库名称: edufee_db
-- 字符集: utf8mb4
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `edufee_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `edufee_db`;

-- ============================================================
-- 1. 系统用户表 (sys_user)
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `role_code` VARCHAR(50) NOT NULL DEFAULT 'STAFF' COMMENT '角色编码: ADMIN-管理员 FINANCE-财务 STAFF-普通员工 TEACHER-教师',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ============================================================
-- 2. 校区表 (campus)
-- ============================================================
DROP TABLE IF EXISTS `campus`;
CREATE TABLE `campus` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '校区名称',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '校区地址',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '校区电话',
    `principal` VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-关闭 1-运营中',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区表';

-- ============================================================
-- 3. 教师表 (edu_teacher)
-- ============================================================
DROP TABLE IF EXISTS `edu_teacher`;
CREATE TABLE `edu_teacher` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `teacher_no` VARCHAR(30) NOT NULL COMMENT '教师编号',
    `name` VARCHAR(50) NOT NULL COMMENT '教师姓名',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `education` VARCHAR(50) DEFAULT NULL COMMENT '学历',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `hire_date` DATE DEFAULT NULL COMMENT '入职日期',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-离职 1-在职 2-休假',
    `specialty` VARCHAR(200) DEFAULT NULL COMMENT '教学特长',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_teacher_no` (`teacher_no`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- ============================================================
-- 4. 课程表 (edu_course)
-- ============================================================
DROP TABLE IF EXISTS `edu_course`;
CREATE TABLE `edu_course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_no` VARCHAR(30) NOT NULL COMMENT '课程编号',
    `name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `category` VARCHAR(50) NOT NULL COMMENT '课程分类(如: 学科辅导/艺术培训/体育培训/语言培训)',
    `age_group` VARCHAR(30) DEFAULT NULL COMMENT '适龄段(如: 3-6岁/6-12岁/12-18岁/成人)',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '课程简介',
    `total_hours` INT DEFAULT 0 COMMENT '总课时数',
    `price_type` VARCHAR(20) NOT NULL DEFAULT 'COURSE' COMMENT '收费模式: COURSE-按课程收费 HOUR-按课时收费 PERIOD-按期收费',
    `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价(元)',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_no` (`course_no`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ============================================================
-- 5. 班级表 (edu_class)
-- ============================================================
DROP TABLE IF EXISTS `edu_class`;
CREATE TABLE `edu_class` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_no` VARCHAR(30) NOT NULL COMMENT '班级编号',
    `name` VARCHAR(100) NOT NULL COMMENT '班级名称',
    `course_id` BIGINT NOT NULL COMMENT '关联课程ID',
    `teacher_id` BIGINT NOT NULL COMMENT '授课教师ID',
    `campus_id` BIGINT NOT NULL COMMENT '所属校区ID',
    `max_students` INT NOT NULL DEFAULT 30 COMMENT '最大学员数',
    `current_students` INT NOT NULL DEFAULT 0 COMMENT '当前学员数',
    `classroom` VARCHAR(100) DEFAULT NULL COMMENT '教室/场地',
    `start_date` DATE DEFAULT NULL COMMENT '开课日期',
    `end_date` DATE DEFAULT NULL COMMENT '结课日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待开课 IN_PROGRESS-进行中 FINISHED-已结课 CANCELED-已取消',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_class_no` (`class_no`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_campus_id` (`campus_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ============================================================
-- 6. 学员表 (edu_student)
-- ============================================================
DROP TABLE IF EXISTS `edu_student`;
CREATE TABLE `edu_student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_no` VARCHAR(30) NOT NULL COMMENT '学员编号',
    `name` VARCHAR(50) NOT NULL COMMENT '学员姓名',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人姓名',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系人电话',
    `contact_relation` VARCHAR(20) DEFAULT NULL COMMENT '与学员关系(如: 父亲/母亲/其他)',
    `school` VARCHAR(100) DEFAULT NULL COMMENT '就读学校',
    `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '家庭地址',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '来源渠道(如: 地推/转介绍/线上广告)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'STUDYING' COMMENT '状态: STUDYING-在读 SUSPENDED-停课 TRANSFERRED-转班 WITHDRAWN-退学 GRADUATED-毕业',
    `campus_id` BIGINT DEFAULT NULL COMMENT '所属校区ID',
    `class_id` BIGINT DEFAULT NULL COMMENT '当前班级ID',
    `enroll_date` DATE DEFAULT NULL COMMENT '入学日期',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_campus_id` (`campus_id`),
    KEY `idx_class_id` (`class_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学员表';

-- ============================================================
-- 7. 排课表 (edu_schedule)
-- ============================================================
DROP TABLE IF EXISTS `edu_schedule`;
CREATE TABLE `edu_schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_id` BIGINT NOT NULL COMMENT '班级ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
    `campus_id` BIGINT NOT NULL COMMENT '校区ID',
    `classroom` VARCHAR(100) NOT NULL COMMENT '教室/场地',
    `schedule_date` DATE NOT NULL COMMENT '上课日期',
    `start_time` TIME NOT NULL COMMENT '上课开始时间',
    `end_time` TIME NOT NULL COMMENT '上课结束时间',
    `week_day` TINYINT NOT NULL COMMENT '星期几(1-7)',
    `lesson_type` VARCHAR(20) DEFAULT 'REGULAR' COMMENT '课次类型: REGULAR-常规课 MAKEUP-补课 TRIAL-试听课',
    `status` VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态: SCHEDULED-已排课 IN_PROGRESS-进行中 FINISHED-已完成 CANCELED-已取消',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_class_id` (`class_id`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_campus_id` (`campus_id`),
    KEY `idx_schedule_date` (`schedule_date`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课表';

-- ============================================================
-- 8. 考勤表 (edu_attendance)
-- ============================================================
DROP TABLE IF EXISTS `edu_attendance`;
CREATE TABLE `edu_attendance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `schedule_id` BIGINT NOT NULL COMMENT '排课ID',
    `student_id` BIGINT NOT NULL COMMENT '学员ID',
    `class_id` BIGINT NOT NULL COMMENT '班级ID',
    `attendance_date` DATE NOT NULL COMMENT '考勤日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PRESENT' COMMENT '状态: PRESENT-出勤 LATE-迟到 ABSENT-缺勤 LEAVE-请假 EARLY_LEAVE-早退',
    `arrive_time` DATETIME DEFAULT NULL COMMENT '到校时间',
    `leave_time` DATETIME DEFAULT NULL COMMENT '离校时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_schedule_student` (`schedule_id`, `student_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_class_id` (`class_id`),
    KEY `idx_attendance_date` (`attendance_date`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤表';

-- ============================================================
-- 9. 收费记录表 (edu_fee_record)
-- ============================================================
DROP TABLE IF EXISTS `edu_fee_record`;
CREATE TABLE `edu_fee_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `fee_no` VARCHAR(50) NOT NULL COMMENT '收费单号',
    `student_id` BIGINT NOT NULL COMMENT '学员ID',
    `student_name` VARCHAR(50) NOT NULL COMMENT '学员姓名(冗余)',
    `class_id` BIGINT DEFAULT NULL COMMENT '班级ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称(冗余)',
    `fee_type` VARCHAR(30) NOT NULL COMMENT '收费类型: TUITION-学费 MATERIAL-教材费 ACTIVITY-活动费 OTHER-其他',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '应收金额(元)',
    `discount_type` VARCHAR(20) DEFAULT NULL COMMENT '优惠类型: NONE-无优惠 FIXED-固定减免 PERCENT-百分比折扣 SPECIAL-特批',
    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额(元)',
    `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实缴金额(元)',
    `paid_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已缴金额(元)',
    `unpaid_amount` DECIMAL(10,2) NOT NULL COMMENT '未缴金额(元)',
    `payment_status` VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT '缴费状态: UNPAID-未缴费 PARTIAL-部分缴费 PAID-已缴清 REFUNDING-退款中 REFUNDED-已退款',
    `due_date` DATE DEFAULT NULL COMMENT '缴费截止日期',
    `pay_date` DATE DEFAULT NULL COMMENT '最后缴费日期',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fee_no` (`fee_no`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_class_id` (`class_id`),
    KEY `idx_payment_status` (`payment_status`),
    KEY `idx_due_date` (`due_date`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收费记录表';

-- ============================================================
-- 10. 缴费明细表 (edu_payment)
-- ============================================================
DROP TABLE IF EXISTS `edu_payment`;
CREATE TABLE `edu_payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `payment_no` VARCHAR(50) NOT NULL COMMENT '缴费流水号',
    `fee_record_id` BIGINT NOT NULL COMMENT '关联收费记录ID',
    `student_id` BIGINT NOT NULL COMMENT '学员ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '缴费金额(元)',
    `payment_method` VARCHAR(30) NOT NULL COMMENT '缴费方式: CASH-现金 WECHAT-微信 ALIPAY-支付宝 BANK_TRANSFER-银行转账 POS-POS机 OTHER-其他',
    `payment_time` DATETIME NOT NULL COMMENT '缴费时间',
    `transaction_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易流水号',
    `receipt_no` VARCHAR(50) DEFAULT NULL COMMENT '收据编号',
    `receipt_url` VARCHAR(255) DEFAULT NULL COMMENT '收据文件URL',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID(收款人)',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_fee_record_id` (`fee_record_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_payment_time` (`payment_time`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费明细表';

-- ============================================================
-- 11. 退款记录表 (edu_refund)
-- ============================================================
DROP TABLE IF EXISTS `edu_refund`;
CREATE TABLE `edu_refund` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `refund_no` VARCHAR(50) NOT NULL COMMENT '退款单号',
    `fee_record_id` BIGINT NOT NULL COMMENT '关联收费记录ID',
    `payment_id` BIGINT NOT NULL COMMENT '关联缴费记录ID',
    `student_id` BIGINT NOT NULL COMMENT '学员ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额(元)',
    `refund_method` VARCHAR(30) NOT NULL COMMENT '退款方式: CASH-现金 WECHAT-微信 ALIPAY-支付宝 BANK_TRANSFER-银行转账',
    `refund_reason` VARCHAR(500) NOT NULL COMMENT '退款原因',
    `refund_time` DATETIME NOT NULL COMMENT '退款时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审核 APPROVED-已通过 REJECTED-已驳回 COMPLETED-已完成',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `approver_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_fee_record_id` (`fee_record_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- ============================================================
-- 12. 催缴记录表 (edu_dunning)
-- ============================================================
DROP TABLE IF EXISTS `edu_dunning`;
CREATE TABLE `edu_dunning` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `fee_record_id` BIGINT NOT NULL COMMENT '关联收费记录ID',
    `student_id` BIGINT NOT NULL COMMENT '学员ID',
    `dunning_date` DATE NOT NULL COMMENT '催缴日期',
    `dunning_method` VARCHAR(30) NOT NULL COMMENT '催缴方式: PHONE-电话 SMS-短信 WECHAT-微信 NOTICE-书面通知',
    `dunning_content` VARCHAR(500) DEFAULT NULL COMMENT '催缴内容',
    `result` VARCHAR(50) DEFAULT NULL COMMENT '催缴结果',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    `next_dunning_date` DATE DEFAULT NULL COMMENT '下次催缴日期',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_fee_record_id` (`fee_record_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_dunning_date` (`dunning_date`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催缴记录表';

-- ============================================================
-- 13. 收据模板表 (edu_receipt_template)
-- ============================================================
DROP TABLE IF EXISTS `edu_receipt_template`;
CREATE TABLE `edu_receipt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_content` TEXT NOT NULL COMMENT '模板内容(HTML格式)',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认: 0-否 1-是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收据模板表';

-- ============================================================
-- 14. 操作日志表 (sys_operation_log)
-- ============================================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    `module` VARCHAR(50) NOT NULL COMMENT '操作模块',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE/UPDATE/DELETE/QUERY/LOGIN/LOGOUT等',
    `target` VARCHAR(200) DEFAULT NULL COMMENT '操作对象',
    `target_id` BIGINT DEFAULT NULL COMMENT '操作对象ID',
    `request_ip` VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
    `request_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数(JSON)',
    `response_result` TEXT DEFAULT NULL COMMENT '响应结果(JSON)',
    `cost_time` BIGINT DEFAULT NULL COMMENT '耗时(毫秒)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
    `error_msg` VARCHAR(2000) DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 15. 字典表 (sys_dict)
-- ============================================================
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
    `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_code` (`dict_type`, `dict_code`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

-- ============================================================
-- 初始化系统管理员账号 (密码: admin123, BCrypt加密)
-- ============================================================
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `role_code`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 'admin@edufee.com', 'ADMIN', 1);

-- 初始化字典数据
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `sort_order`) VALUES
('GENDER', '1', '男', 1),
('GENDER', '2', '女', 2),
('STUDENT_STATUS', 'STUDYING', '在读', 1),
('STUDENT_STATUS', 'SUSPENDED', '停课', 2),
('STUDENT_STATUS', 'TRANSFERRED', '转班', 3),
('STUDENT_STATUS', 'WITHDRAWN', '退学', 4),
('STUDENT_STATUS', 'GRADUATED', '毕业', 5),
('CLASS_STATUS', 'PENDING', '待开课', 1),
('CLASS_STATUS', 'IN_PROGRESS', '进行中', 2),
('CLASS_STATUS', 'FINISHED', '已结课', 3),
('CLASS_STATUS', 'CANCELED', '已取消', 4),
('FEE_TYPE', 'TUITION', '学费', 1),
('FEE_TYPE', 'MATERIAL', '教材费', 2),
('FEE_TYPE', 'ACTIVITY', '活动费', 3),
('FEE_TYPE', 'OTHER', '其他', 4),
('PAYMENT_METHOD', 'CASH', '现金', 1),
('PAYMENT_METHOD', 'WECHAT', '微信', 2),
('PAYMENT_METHOD', 'ALIPAY', '支付宝', 3),
('PAYMENT_METHOD', 'BANK_TRANSFER', '银行转账', 4),
('PAYMENT_METHOD', 'POS', 'POS机', 5),
('PAYMENT_STATUS', 'UNPAID', '未缴费', 1),
('PAYMENT_STATUS', 'PARTIAL', '部分缴费', 2),
('PAYMENT_STATUS', 'PAID', '已缴清', 3),
('PAYMENT_STATUS', 'REFUNDING', '退款中', 4),
('PAYMENT_STATUS', 'REFUNDED', '已退款', 5),
('ATTENDANCE_STATUS', 'PRESENT', '出勤', 1),
('ATTENDANCE_STATUS', 'LATE', '迟到', 2),
('ATTENDANCE_STATUS', 'ABSENT', '缺勤', 3),
('ATTENDANCE_STATUS', 'LEAVE', '请假', 4),
('ATTENDANCE_STATUS', 'EARLY_LEAVE', '早退', 5);
