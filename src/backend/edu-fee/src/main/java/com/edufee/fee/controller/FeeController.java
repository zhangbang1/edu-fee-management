package com.edufee.fee.controller;

import com.edufee.common.PageDTO;
import com.edufee.common.R;
import com.edufee.fee.entity.FeeRecord;
import com.edufee.fee.entity.Payment;
import com.edufee.fee.service.FeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 收费管理控制器
 * 提供收费记录、缴费、退款、催缴等接口
 * 财务操作需 ADMIN 或 FINANCE 角色权限
 */
@Slf4j
@RestController
@RequestMapping("/api/fee")
@Tag(name = "收费管理", description = "收费记录、缴费、退款、催缴管理")
public class FeeController {

    @Resource
    private FeeService feeService;

    // ==================== 收费记录 ====================

    @GetMapping("/records")
    @Operation(summary = "分页查询收费记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'STAFF')")
    public R<PageDTO<FeeRecord>> listRecords(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String feeType) {
        return R.ok(feeService.listFeeRecords(page, size, studentId, paymentStatus, feeType));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "查询学员收费记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'STAFF')")
    public R<List<FeeRecord>> listByStudent(@PathVariable Long studentId) {
        return R.ok(feeService.listFeeRecordsByStudent(studentId));
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "获取收费记录详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'STAFF')")
    public R<FeeRecord> getRecordById(@PathVariable Long id) {
        return R.ok(feeService.getFeeRecordById(id));
    }

    @PostMapping("/records")
    @Operation(summary = "创建收费记录（应收账单）")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public R<FeeRecord> createRecord(@RequestBody FeeRecord feeRecord) {
        return R.ok("收费记录创建成功", feeService.createFeeRecord(feeRecord));
    }

    // ==================== 缴费管理 ====================

    @PostMapping("/payment")
    @Operation(summary = "发起缴费", description = "为收费记录缴纳费用，使用BigDecimal精确计算")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public R<Payment> makePayment(@RequestBody Map<String, Object> params) {
        Long feeRecordId = Long.valueOf(params.get("feeRecordId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String paymentMethod = (String) params.get("paymentMethod");
        String transactionNo = (String) params.getOrDefault("transactionNo", null);
        Long operatorId = Long.valueOf(params.get("operatorId").toString());
        String operatorName = (String) params.get("operatorName");

        Payment payment = feeService.makePayment(feeRecordId, amount, paymentMethod, transactionNo, operatorId, operatorName);
        return R.ok("缴费成功", payment);
    }

    @GetMapping("/payment/list")
    @Operation(summary = "分页查询缴费记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'STAFF')")
    public R<PageDTO<Payment>> listPayments(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long studentId) {
        return R.ok(feeService.listPayments(page, size, studentId));
    }

    @GetMapping("/records/{feeRecordId}/payments")
    @Operation(summary = "查询收费记录的缴费明细")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'STAFF')")
    public R<List<Payment>> listPaymentsByRecord(@PathVariable Long feeRecordId) {
        return R.ok(feeService.listPaymentsByFeeRecord(feeRecordId));
    }

    // ==================== 退款管理 ====================

    @PostMapping("/refund/apply")
    @Operation(summary = "申请退款")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public R<Void> applyRefund(@RequestBody Map<String, Object> params) {
        Long feeRecordId = Long.valueOf(params.get("feeRecordId").toString());
        Long paymentId = Long.valueOf(params.get("paymentId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String reason = (String) params.get("reason");
        Long operatorId = Long.valueOf(params.get("operatorId").toString());
        String operatorName = (String) params.get("operatorName");

        feeService.applyRefund(feeRecordId, paymentId, amount, reason, operatorId, operatorName);
        return R.ok("退款申请已提交");
    }

    @PostMapping("/refund/approve")
    @Operation(summary = "审批退款")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> approveRefund(@RequestBody Map<String, Object> params) {
        Long refundId = Long.valueOf(params.get("refundId").toString());
        Long approverId = Long.valueOf(params.get("approverId").toString());
        String approverName = (String) params.get("approverName");
        boolean approved = (Boolean) params.get("approved");
        String comment = (String) params.getOrDefault("comment", "");

        feeService.approveRefund(refundId, approverId, approverName, approved, comment);
        return R.ok(approved ? "退款已通过" : "退款已驳回");
    }

    // ==================== 催缴管理 ====================

    @PostMapping("/dunning")
    @Operation(summary = "生成催缴记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public R<Void> createDunning(@RequestBody Map<String, Object> params) {
        Long feeRecordId = Long.valueOf(params.get("feeRecordId").toString());
        String method = (String) params.get("method");
        String content = (String) params.getOrDefault("content", "请尽快缴纳费用");
        Long operatorId = Long.valueOf(params.get("operatorId").toString());
        String operatorName = (String) params.get("operatorName");

        feeService.createDunning(feeRecordId, method, content, operatorId, operatorName);
        return R.ok("催缴记录已生成");
    }

    @GetMapping("/dunning/overdue")
    @Operation(summary = "查询欠费学员列表", description = "查询已超过缴费截止日期的未缴/部分缴费记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public R<List<FeeRecord>> listOverdue(
            @RequestParam(required = false) Long campusId) {
        return R.ok(feeService.listOverdueRecords(campusId));
    }

}
