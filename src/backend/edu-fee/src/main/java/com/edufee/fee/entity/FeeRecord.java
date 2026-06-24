package com.edufee.fee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 收费记录实体
 * 对应数据库 edu_fee_record 表，记录每一笔应收费用
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_fee_record")
public class FeeRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 收费单号（唯一） */
    private String feeNo;

    /** 学员ID */
    private Long studentId;

    /** 学员姓名（冗余） */
    private String studentName;

    /** 班级ID */
    private Long classId;

    /** 课程ID */
    private Long courseId;

    /** 课程名称（冗余） */
    private String courseName;

    /** 收费类型: TUITION-学费 MATERIAL-教材费 ACTIVITY-活动费 OTHER-其他 */
    private String feeType;

    /** 应收金额（元） */
    private BigDecimal amount;

    /** 优惠类型: NONE-无优惠 FIXED-固定减免 PERCENT-百分比折扣 SPECIAL-特批 */
    private String discountType;

    /** 优惠金额（元） */
    private BigDecimal discountAmount;

    /** 实缴金额（元）= amount - discountAmount */
    private BigDecimal actualAmount;

    /** 已缴金额（元） */
    private BigDecimal paidAmount;

    /** 未缴金额（元）= actualAmount - paidAmount */
    private BigDecimal unpaidAmount;

    /** 缴费状态: UNPAID-未缴费 PARTIAL-部分缴费 PAID-已缴清 REFUNDING-退款中 REFUNDED-已退款 */
    private String paymentStatus;

    /** 缴费截止日期 */
    private LocalDate dueDate;

    /** 最后缴费日期 */
    private LocalDate payDate;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 备注 */
    private String remark;

}
