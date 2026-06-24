package com.edufee.report.controller;

import com.edufee.common.R;
import com.edufee.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Map;

/**
 * 报表统计控制器
 * 提供收费、学员、课程、班级等多维度统计报表接口
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@Tag(name = "报表统计", description = "收费报表、学员报表、课程收入、班级满员、月度趋势")
public class ReportController {

    @Resource
    private ReportService reportService;

    /**
     * 收费统计报表
     */
    @GetMapping("/fee")
    @Operation(summary = "收费统计报表", description = "统计指定时间段的收费汇总数据")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public R<Map<String, Object>> feeStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long campusId) {
        return R.ok(reportService.feeStatistics(startDate, endDate, campusId));
    }

    /**
     * 学员统计报表
     */
    @GetMapping("/student")
    @Operation(summary = "学员统计报表", description = "统计学员总数及状态分布")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'STAFF')")
    public R<Map<String, Object>> studentStatistics(
            @RequestParam(required = false) Long campusId) {
        return R.ok(reportService.studentStatistics(campusId));
    }

    /**
     * 课程收入排名报表
     */
    @GetMapping("/course-revenue")
    @Operation(summary = "课程收入排名报表", description = "按课程统计收入排名")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public R<Map<String, Object>> courseRevenue(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return R.ok(reportService.courseRevenueReport(startDate, endDate));
    }

    /**
     * 班级满员率报表
     */
    @GetMapping("/class-occupation")
    @Operation(summary = "班级满员率报表", description = "统计各班型的满员率")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'STAFF')")
    public R<Map<String, Object>> classOccupation(
            @RequestParam(required = false) Long campusId) {
        return R.ok(reportService.classOccupationReport(campusId));
    }

    /**
     * 月度收费趋势报表
     */
    @GetMapping("/monthly-trend")
    @Operation(summary = "月度收费趋势报表", description = "按12个月统计收费趋势")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public R<Map<String, Object>> monthlyTrend(
            @RequestParam(defaultValue = "2025") int year) {
        return R.ok(reportService.monthlyFeeTrend(year));
    }

}
