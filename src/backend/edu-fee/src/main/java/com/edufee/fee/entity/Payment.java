package com.edufee.fee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费明细实体
 * 对应数据库 edu_payment 表，记录每一笔实际缴费
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_payment")
public class Payment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 缴费流水号（唯一） */
    private String paymentNo;

    /** 关联收费记录ID */
    private Long feeRecordId;

    /** 学员ID */
    private Long studentId;

    /** 缴费金额（元） */
    private BigDecimal amount;

    /** 缴费方式: CASH/WECHAT/ALIPAY/BANK_TRANSFER/POS/OTHER */
    private String paymentMethod;

    /** 缴费时间 */
    private LocalDateTime paymentTime;

    /** 第三方交易流水号 */
    private String transactionNo;

    /** 收据编号 */
    private String receiptNo;

    /** 收据文件URL */
    private String receiptUrl;

    /** 操作人ID（收款人） */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 备注 */
    private String remark;

}
