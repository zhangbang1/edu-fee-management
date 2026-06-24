package com.edufee.common;

/**
 * 系统常量类
 * 集中管理系统中所有常量定义，避免魔法值散落各处
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    // ============ 通用常量 ============

    /** 系统默认日期格式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /** 系统默认日期时间格式 */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 默认每页条数 */
    public static final long DEFAULT_PAGE_SIZE = 10;

    /** 最大每页条数 */
    public static final long MAX_PAGE_SIZE = 100;

    // ============ 用户角色 ============

    /** 系统管理员 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 财务人员 */
    public static final String ROLE_FINANCE = "FINANCE";

    /** 普通员工 */
    public static final String ROLE_STAFF = "STAFF";

    /** 教师 */
    public static final String ROLE_TEACHER = "TEACHER";

    // ============ 学员状态 ============

    /** 在读 */
    public static final String STUDENT_STATUS_STUDYING = "STUDYING";

    /** 停课 */
    public static final String STUDENT_STATUS_SUSPENDED = "SUSPENDED";

    /** 转班 */
    public static final String STUDENT_STATUS_TRANSFERRED = "TRANSFERRED";

    /** 退学 */
    public static final String STUDENT_STATUS_WITHDRAWN = "WITHDRAWN";

    /** 毕业 */
    public static final String STUDENT_STATUS_GRADUATED = "GRADUATED";

    // ============ 班级状态 ============

    /** 待开课 */
    public static final String CLASS_STATUS_PENDING = "PENDING";

    /** 进行中 */
    public static final String CLASS_STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** 已结课 */
    public static final String CLASS_STATUS_FINISHED = "FINISHED";

    /** 已取消 */
    public static final String CLASS_STATUS_CANCELED = "CANCELED";

    // ============ 缴费状态 ============

    /** 未缴费 */
    public static final String PAYMENT_STATUS_UNPAID = "UNPAID";

    /** 部分缴费 */
    public static final String PAYMENT_STATUS_PARTIAL = "PARTIAL";

    /** 已缴清 */
    public static final String PAYMENT_STATUS_PAID = "PAID";

    /** 退款中 */
    public static final String PAYMENT_STATUS_REFUNDING = "REFUNDING";

    /** 已退款 */
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";

    // ============ 收费模式 ============

    /** 按课程收费 */
    public static final String PRICE_TYPE_COURSE = "COURSE";

    /** 按课时收费 */
    public static final String PRICE_TYPE_HOUR = "HOUR";

    /** 按期收费 */
    public static final String PRICE_TYPE_PERIOD = "PERIOD";

    // ============ 考勤状态 ============

    /** 出勤 */
    public static final String ATTENDANCE_PRESENT = "PRESENT";

    /** 迟到 */
    public static final String ATTENDANCE_LATE = "LATE";

    /** 缺勤 */
    public static final String ATTENDANCE_ABSENT = "ABSENT";

    /** 请假 */
    public static final String ATTENDANCE_LEAVE = "LEAVE";

    /** 早退 */
    public static final String ATTENDANCE_EARLY_LEAVE = "EARLY_LEAVE";

    // ============ 排课状态 ============

    /** 已排课 */
    public static final String SCHEDULE_STATUS_SCHEDULED = "SCHEDULED";

    /** 进行中 */
    public static final String SCHEDULE_STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** 已完成 */
    public static final String SCHEDULE_STATUS_FINISHED = "FINISHED";

    /** 已取消 */
    public static final String SCHEDULE_STATUS_CANCELED = "CANCELED";

    // ============ 退款状态 ============

    /** 待审核 */
    public static final String REFUND_STATUS_PENDING = "PENDING";

    /** 已通过 */
    public static final String REFUND_STATUS_APPROVED = "APPROVED";

    /** 已驳回 */
    public static final String REFUND_STATUS_REJECTED = "REJECTED";

    /** 已完成 */
    public static final String REFUND_STATUS_COMPLETED = "COMPLETED";

    // ============ 收费类型 ============

    /** 学费 */
    public static final String FEE_TYPE_TUITION = "TUITION";

    /** 教材费 */
    public static final String FEE_TYPE_MATERIAL = "MATERIAL";

    /** 活动费 */
    public static final String FEE_TYPE_ACTIVITY = "ACTIVITY";

    /** 其他 */
    public static final String FEE_TYPE_OTHER = "OTHER";

}
