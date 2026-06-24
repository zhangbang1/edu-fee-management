package com.edufee.fee.util;

import com.edufee.fee.entity.Payment;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 收据生成工具类
 * 负责生成缴费收据的HTML内容，支持打印
 */
@Slf4j
public class ReceiptUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

    /**
     * 生成收据编号
     * 格式: RCP + yyyyMMddHHmmss + 4位随机数
     *
     * @return 收据编号
     */
    public static String generateReceiptNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = cn.hutool.core.util.RandomUtil.randomNumbers(4);
        return "RCP" + datePart + randomPart;
    }

    /**
     * 生成收据HTML内容
     * 用于前端展示和打印
     *
     * @param payment     缴费记录
     * @param studentName 学员姓名
     * @param courseName  课程名称
     * @return HTML格式收据
     */
    public static String generateReceiptHtml(Payment payment, String studentName, String courseName) {
        StringBuilder html = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>缴费收据</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'SimSun', serif; margin: 40px; }\n");
        html.append(".receipt { border: 2px solid #333; padding: 30px; max-width: 600px; margin: 0 auto; }\n");
        html.append(".header { text-align: center; border-bottom: 1px dashed #333; padding-bottom: 15px; margin-bottom: 20px; }\n");
        html.append(".header h2 { margin: 0 0 5px 0; }\n");
        html.append(".receipt-no { text-align: right; font-size: 12px; color: #666; margin-bottom: 20px; }\n");
        html.append(".info-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }\n");
        html.append(".info-table td { padding: 8px 10px; border-bottom: 1px dotted #ddd; }\n");
        html.append(".info-table .label { width: 120px; font-weight: bold; }\n");
        html.append(".amount { font-size: 18px; font-weight: bold; color: #d00; }\n");
        html.append(".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #999; }\n");
        html.append(".signature { margin-top: 40px; text-align: right; }\n");
        html.append("@media print { body { margin: 0; } .no-print { display: none; } }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"receipt\">\n");

        // 标题
        html.append("<div class=\"header\">\n");
        html.append("<h2>教育培训机构收费收据</h2>\n");
        html.append("<p>EduFee Management System</p>\n");
        html.append("</div>\n");

        // 收据编号
        html.append("<div class=\"receipt-no\">\n");
        html.append("收据编号: ").append(payment.getReceiptNo() != null ? payment.getReceiptNo() : "N/A").append("<br>\n");
        html.append("打印时间: ").append(now.format(DATETIME_FMT)).append("\n");
        html.append("</div>\n");

        // 基本信息表格
        html.append("<table class=\"info-table\">\n");
        html.append("<tr><td class=\"label\">学员姓名:</td><td>").append(escapeHtml(studentName)).append("</td></tr>\n");
        html.append("<tr><td class=\"label\">课程名称:</td><td>").append(escapeHtml(courseName)).append("</td></tr>\n");
        html.append("<tr><td class=\"label\">缴费金额:</td><td class=\"amount\">").append(formatAmount(payment.getAmount())).append("</td></tr>\n");
        html.append("<tr><td class=\"label\">缴费方式:</td><td>").append(getMethodText(payment.getPaymentMethod())).append("</td></tr>\n");
        html.append("<tr><td class=\"label\">缴费时间:</td><td>").append(payment.getPaymentTime() != null ? payment.getPaymentTime().format(DATETIME_FMT) : "").append("</td></tr>\n");
        html.append("<tr><td class=\"label\">交易流水号:</td><td>").append(payment.getTransactionNo() != null ? payment.getTransactionNo() : "N/A").append("</td></tr>\n");
        html.append("<tr><td class=\"label\">收款人:</td><td>").append(escapeHtml(payment.getOperatorName())).append("</td></tr>\n");
        html.append("</table>\n");

        // 金额大写
        html.append("<div style=\"margin: 10px 0;\">\n");
        html.append("<strong>金额（大写）: </strong>").append(convertToChinese(payment.getAmount())).append("\n");
        html.append("</div>\n");

        html.append("<p style=\"color: #666; font-size: 12px;\">本收据由EduFeeMS系统生成，可作为缴费凭证。</p>\n");

        // 签名区域
        html.append("<div class=\"signature\">\n");
        html.append("<p>收款单位（盖章）: ___________________</p>\n");
        html.append("<p>收款人签名: ___________________</p>\n");
        html.append("<p>日期: ").append(now.format(DATE_FMT)).append("</p>\n");
        html.append("</div>\n");

        // 页脚
        html.append("<div class=\"footer\">\n");
        html.append("<p>EduFeeMS - 教育培训机构教务收费管理系统</p>\n");
        html.append("</div>\n");

        html.append("</div>\n");

        // 打印按钮
        html.append("<div class=\"no-print\" style=\"text-align: center; margin-top: 20px;\">\n");
        html.append("<button onclick=\"window.print()\" style=\"padding: 10px 30px; font-size: 16px; cursor: pointer;\">打印收据</button>\n");
        html.append("</div>\n");

        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    /**
     * HTML转义处理
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    /**
     * 格式化金额为 ￥xx.00 格式
     */
    private static String formatAmount(BigDecimal amount) {
        if (amount == null) return "￥0.00";
        return "￥" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }

    /**
     * 获取缴费方式中文描述
     */
    private static String getMethodText(String method) {
        switch (method) {
            case "CASH": return "现金";
            case "WECHAT": return "微信支付";
            case "ALIPAY": return "支付宝";
            case "BANK_TRANSFER": return "银行转账";
            case "POS": return "POS机刷卡";
            case "OTHER": return "其他";
            default: return method;
        }
    }

    /**
     * 将金额转换为中文大写
     * 使用Hutool工具类简化实现
     */
    private static String convertToChinese(BigDecimal amount) {
        if (amount == null) return "零元整";
        return cn.hutool.core.util.NumberUtil.toChinese(
                amount.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue(), false);
    }

}
