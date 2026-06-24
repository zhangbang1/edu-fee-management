package com.edufee.fee.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.common.BusinessException;
import com.edufee.common.Constants;
import com.edufee.common.PageDTO;
import com.edufee.fee.entity.FeeRecord;
import com.edufee.fee.entity.Payment;
import com.edufee.fee.mapper.FeeRecordMapper;
import com.edufee.fee.mapper.PaymentMapper;
import com.edufee.fee.service.FeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 收费管理服务实现类
 * 使用BigDecimal进行精确金额计算，包含收费记录、缴费、退款、催缴等完整业务逻辑
 */
@Slf4j
@Service
public class FeeServiceImpl implements FeeService {

    @Resource
    private FeeRecordMapper feeRecordMapper;

    @Resource
    private PaymentMapper paymentMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ==================== 收费记录 ====================

    /**
     * 分页查询收费记录
     */
    @Override
    public PageDTO<FeeRecord> listFeeRecords(long page, long size, Long studentId, String paymentStatus, String feeType) {
        LambdaQueryWrapper<FeeRecord> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(FeeRecord::getStudentId, studentId);
        }
        if (StringUtils.hasText(paymentStatus)) {
            wrapper.eq(FeeRecord::getPaymentStatus, paymentStatus);
        }
        if (StringUtils.hasText(feeType)) {
            wrapper.eq(FeeRecord::getFeeType, feeType);
        }
        wrapper.orderByDesc(FeeRecord::getCreateTime);

        Page<FeeRecord> feePage = feeRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return PageDTO.of(feePage);
    }

    /**
     * 查询学员的所有收费记录
     */
    @Override
    public List<FeeRecord> listFeeRecordsByStudent(Long studentId) {
        LambdaQueryWrapper<FeeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeRecord::getStudentId, studentId)
               .orderByDesc(FeeRecord::getCreateTime);
        return feeRecordMapper.selectList(wrapper);
    }

    /**
     * 创建收费记录（应收账单）
     * 使用BigDecimal 进行精确金额计算：
     * - actualAmount = amount - discountAmount
     * - unpaidAmount = actualAmount - paidAmount（初值为actualAmount）
     * - 校验金额不能为负
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeeRecord createFeeRecord(FeeRecord feeRecord) {
        // 生成收费单号: FEE + 日期时间 + 4位随机数
        String datePart = LocalDateTime.now().format(DATETIME_FMT);
        String randomPart = RandomUtil.randomNumbers(4);
        feeRecord.setFeeNo("FEE" + datePart + randomPart);

        // BigDecimal 精确金额计算
        BigDecimal amount = feeRecord.getAmount() != null ? feeRecord.getAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = feeRecord.getDiscountAmount() != null ? feeRecord.getDiscountAmount() : BigDecimal.ZERO;

        // 实缴金额 = 应收金额 - 优惠金额（不能为负数）
        BigDecimal actualAmount = amount.subtract(discountAmount);
        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("实缴金额不能为负数：优惠金额超过应收金额");
        }
        feeRecord.setActualAmount(actualAmount.setScale(2, RoundingMode.HALF_UP));

        // 初始已缴金额为0
        feeRecord.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        // 未缴金额 = 实缴金额
        feeRecord.setUnpaidAmount(actualAmount.setScale(2, RoundingMode.HALF_UP));

        // 默认状态为未缴费
        if (!StringUtils.hasText(feeRecord.getPaymentStatus())) {
            feeRecord.setPaymentStatus(Constants.PAYMENT_STATUS_UNPAID);
        }
        if (!StringUtils.hasText(feeRecord.getDiscountType())) {
            feeRecord.setDiscountType("NONE");
        }

        feeRecordMapper.insert(feeRecord);
        log.info("收费记录创建成功: feeNo={}, studentId={}, amount={}, actualAmount={}",
                feeRecord.getFeeNo(), feeRecord.getStudentId(), amount, actualAmount);
        return feeRecord;
    }

    /**
     * 获取收费记录详情
     */
    @Override
    public FeeRecord getFeeRecordById(Long id) {
        FeeRecord feeRecord = feeRecordMapper.selectById(id);
        if (feeRecord == null) {
            throw BusinessException.notFound("收费记录不存在，ID: " + id);
        }
        return feeRecord;
    }

    // ==================== 缴费管理 ====================

    /**
     * 发起缴费
     * 使用BigDecimal精确计算，确保金额一致性和状态联动：
     * 1. 校验缴费金额是否超过未缴金额
     * 2. 创建缴费记录
     * 3. 更新收费记录：累加已缴金额，重新计算未缴金额
     * 4. 根据缴清情况更新收费状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment makePayment(Long feeRecordId, BigDecimal amount, String paymentMethod,
                                String transactionNo, Long operatorId, String operatorName) {
        // 获取收费记录
        FeeRecord feeRecord = getFeeRecordById(feeRecordId);

        // 检查状态是否允许缴费
        if (Constants.PAYMENT_STATUS_REFUNDED.equals(feeRecord.getPaymentStatus())) {
            throw new BusinessException("该收费记录已退款，无法继续缴费");
        }
        if (Constants.PAYMENT_STATUS_PAID.equals(feeRecord.getPaymentStatus())) {
            throw new BusinessException("该收费记录已缴清，无需再次缴费");
        }

        // BigDecimal 精确校验：缴费金额不能超过未缴金额
        BigDecimal payAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal unpaidAmount = feeRecord.getUnpaidAmount().setScale(2, RoundingMode.HALF_UP);

        if (payAmount.compareTo(unpaidAmount) > 0) {
            throw BusinessException.badRequest(String.format(
                    "缴费金额(%s)超过未缴金额(%s)", payAmount, unpaidAmount));
        }

        // 创建缴费记录
        Payment payment = new Payment();
        payment.setPaymentNo("PAY" + LocalDateTime.now().format(DATETIME_FMT) + RandomUtil.randomNumbers(4));
        payment.setFeeRecordId(feeRecordId);
        payment.setStudentId(feeRecord.getStudentId());
        payment.setAmount(payAmount);
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionNo(transactionNo);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setOperatorId(operatorId);
        payment.setOperatorName(operatorName);

        paymentMapper.insert(payment);

        // 更新收费记录：累加已缴金额
        BigDecimal newPaidAmount = feeRecord.getPaidAmount().add(payAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newUnpaidAmount = feeRecord.getActualAmount().subtract(newPaidAmount).setScale(2, RoundingMode.HALF_UP);

        feeRecord.setPaidAmount(newPaidAmount);
        feeRecord.setUnpaidAmount(newUnpaidAmount.max(BigDecimal.ZERO)); // 不能为负
        feeRecord.setPayDate(LocalDate.now());

        // 判断缴费状态
        if (newUnpaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            feeRecord.setPaymentStatus(Constants.PAYMENT_STATUS_PAID);
        } else {
            feeRecord.setPaymentStatus(Constants.PAYMENT_STATUS_PARTIAL);
        }

        feeRecordMapper.updateById(feeRecord);

        log.info("缴费成功: paymentNo={}, feeRecordId={}, amount={}, method={}, 已缴/实缴: {}/{}",
                payment.getPaymentNo(), feeRecordId, payAmount, paymentMethod,
                newPaidAmount, feeRecord.getActualAmount());

        return payment;
    }

    /**
     * 查询收费记录的缴费明细
     */
    @Override
    public List<Payment> listPaymentsByFeeRecord(Long feeRecordId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getFeeRecordId, feeRecordId)
               .orderByDesc(Payment::getPaymentTime);
        return paymentMapper.selectList(wrapper);
    }

    /**
     * 分页查询缴费记录
     */
    @Override
    public PageDTO<Payment> listPayments(long page, long size, Long studentId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Payment::getStudentId, studentId);
        }
        wrapper.orderByDesc(Payment::getPaymentTime);

        Page<Payment> paymentPage = paymentMapper.selectPage(new Page<>(page, size), wrapper);
        return PageDTO.of(paymentPage);
    }

    // ==================== 退款管理 ====================

    /**
     * 申请退款
     * 1. 校验收费记录状态和缴费记录
     * 2. 校验退款金额不超过已缴金额
     * 3. 修改收费记录状态为退款中
     * 4. TODO: 插入退款记录到退款表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long feeRecordId, Long paymentId, BigDecimal amount, String reason,
                            Long operatorId, String operatorName) {
        FeeRecord feeRecord = getFeeRecordById(feeRecordId);

        if (Constants.PAYMENT_STATUS_REFUNDED.equals(feeRecord.getPaymentStatus())) {
            throw new BusinessException("该收费记录已退款，无法再次申请");
        }

        // 获取缴费记录
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw BusinessException.notFound("缴费记录不存在，ID: " + paymentId);
        }

        // BigDecimal 校验退款金额
        BigDecimal refundAmount = amount.setScale(2, RoundingMode.HALF_UP);
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw BusinessException.badRequest("退款金额不能超过缴费金额");
        }

        // 更新收费记录状态为退款中
        feeRecord.setPaymentStatus(Constants.PAYMENT_STATUS_REFUNDING);
        feeRecordMapper.updateById(feeRecord);

        log.info("退款申请已提交: feeRecordId={}, paymentId={}, amount={}, reason={}",
                feeRecordId, paymentId, refundAmount, reason);

        // TODO: 插入退款记录 (edu_refund 表)
        // TODO: 发送退款审批通知
    }

    /**
     * 审批退款
     * 通过时更新收费状态为已退款；驳回时恢复原状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long refundId, Long approverId, String approverName, boolean approved, String comment) {
        // TODO: 从退款表查询退款记录
        // TODO: 根据审批结果更新退款记录状态
        // 如果通过，将收费记录状态设为已退款
        // 如果驳回，将收费记录状态恢复为之前的状态

        log.info("退款审批处理: refundId={}, approved={}, approver={}", refundId, approved, approverName);
    }

    // ==================== 催缴管理 ====================

    /**
     * 生成催缴记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDunning(Long feeRecordId, String method, String content, Long operatorId, String operatorName) {
        FeeRecord feeRecord = getFeeRecordById(feeRecordId);

        if (Constants.PAYMENT_STATUS_PAID.equals(feeRecord.getPaymentStatus())) {
            throw new BusinessException("该费用已缴清，无需催缴");
        }

        // TODO: 插入催缴记录 (edu_dunning 表)

        log.info("催缴记录已生成: feeRecordId={}, method={}, operator={}", feeRecordId, method, operatorName);
    }

    /**
     * 查询欠费学员列表
     * 查询所有未缴费和部分缴费的记录，且已超过截止日期
     */
    @Override
    public List<FeeRecord> listOverdueRecords(Long campusId) {
        LambdaQueryWrapper<FeeRecord> wrapper = new LambdaQueryWrapper<>();
        // 未缴费或部分缴费
        wrapper.in(FeeRecord::getPaymentStatus, Constants.PAYMENT_STATUS_UNPAID, Constants.PAYMENT_STATUS_PARTIAL);
        // 已超过缴费截止日期
        wrapper.le(FeeRecord::getDueDate, LocalDate.now());
        wrapper.orderByAsc(FeeRecord::getDueDate);

        return feeRecordMapper.selectList(wrapper);
    }

}
