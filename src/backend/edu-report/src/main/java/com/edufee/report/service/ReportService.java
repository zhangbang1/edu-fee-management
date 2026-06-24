package com.edufee.report.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 报表统计服务接口
 * 提供收费、学员、考勤等多维度统计报表
 */
public interface ReportService {

    /**
     * 收费统计报表
     * 统计指定时间段内的收费金额汇总
     */
    Map<String, Object> feeStatistics(LocalDate startDate, LocalDate endDate, Long campusId);

    /**
     * 学员统计报表
     * 统计学员总数、在读、退学、毕业等数量
     */
    Map<String, Object> studentStatistics(Long campusId);

    /**
     * 课程收入排名报表
     * 按课程统计收入情况
     */
    Map<String, Object> courseRevenueReport(LocalDate startDate, LocalDate endDate);

    /**
     * 班级满员率报表
     * 统计各班型的满员情况和班级数量
     */
    Map<String, Object> classOccupationReport(Long campusId);

    /**
     * 月度收费趋势报表
     * 按月统计收费总额趋势
     */
    Map<String, Object> monthlyFeeTrend(int year);

}
