package com.edufee.fee.service;

import com.edufee.common.PageDTO;
import com.edufee.fee.entity.FeeRecord;
import com.edufee.fee.entity.Payment;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收费管理服务接口
 * 定义收费记录管理、缴费、退款、催缴等核心业务方法
 */
public interface FeeService {

    // ========== 收费记录 ==========

    /** 分页查询收费记录 */
    PageDTO<FeeRecord> listFeeRecords(long page, long size, Long studentId, String paymentStatus, String feeType);

    /** 查询学员的收费记录 */
    List<FeeRecord> listFeeRecordsByStudent(Long studentId);

    /** 创建收费记录（应收账单） */
    FeeRecord createFeeRecord(FeeRecord feeRecord);

    /** 获取收费记录详情 */
    FeeRecord getFeeRecordById(Long id);

    // ========== 缴费管理 ==========

    /** 发起缴费 */
    Payment makePayment(Long feeRecordId, BigDecimal amount, String paymentMethod,
                         String transactionNo, Long operatorId, String operatorName);

    /** 查询收费记录的缴费明细 */
    List<Payment> listPaymentsByFeeRecord(Long feeRecordId);

    /** 分页查询缴费记录 */
    PageDTO<Payment> listPayments(long page, long size, Long studentId);

    // ========== 退款管理 ==========

    /** 申请退款 */
    void applyRefund(Long feeRecordId, Long paymentId, BigDecimal amount, String reason,
                     Long operatorId, String operatorName);

    /** 审批退款 */
    void approveRefund(Long refundId, Long approverId, String approverName, boolean approved, String comment);

    // ========== 催缴管理 ==========

    /** 生成催缴记录 */
    void createDunning(Long feeRecordId, String method, String content, Long operatorId, String operatorName);

    /** 查询欠费学员列表 */
    List<FeeRecord> listOverdueRecords(Long campusId);

}
