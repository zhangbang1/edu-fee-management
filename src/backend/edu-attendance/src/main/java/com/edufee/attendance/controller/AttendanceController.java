package com.edufee.attendance.controller;

import com.edufee.attendance.entity.Attendance;
import com.edufee.attendance.service.AttendanceService;
import com.edufee.common.PageDTO;
import com.edufee.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance")
@Tag(name = "考勤管理", description = "考勤记录查询、签到、统计分析")
public class AttendanceController {

    @Resource
    private AttendanceService attendanceService;

    @GetMapping("/list")
    @Operation(summary = "分页查询考勤记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<PageDTO<Attendance>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return R.ok(attendanceService.listAttendance(page, size, studentId, classId, status, startDate, endDate));
    }

    @GetMapping("/schedule/{scheduleId}")
    @Operation(summary = "查询排课对应的考勤记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public R<List<Attendance>> listBySchedule(@PathVariable Long scheduleId) {
        return R.ok(attendanceService.listByScheduleId(scheduleId));
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建考勤记录", description = "根据排课ID为班级学员生成考勤记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public R<List<Attendance>> batchCreate(@RequestBody Map<String, Object> params) {
        Long scheduleId = Long.valueOf(params.get("scheduleId").toString());
        @SuppressWarnings("unchecked")
        List<Long> studentIds = ((List<Integer>) params.get("studentIds")).stream()
                .map(Integer::longValue).collect(java.util.stream.Collectors.toList());
        return R.ok("考勤记录创建成功", attendanceService.batchCreateAttendance(scheduleId, studentIds));
    }

    @PutMapping("/{attendanceId}")
    @Operation(summary = "更新考勤状态", description = "签到、请假、迟到、缺勤等状态更新")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public R<Void> updateStatus(@PathVariable Long attendanceId,
                                 @RequestParam String status,
                                 @RequestParam(required = false) String remark) {
        attendanceService.updateAttendanceStatus(attendanceId, status, remark);
        return R.ok("考勤状态更新成功");
    }

    @GetMapping("/stats/student/{studentId}")
    @Operation(summary = "学员考勤统计")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<Map<String, Object>> studentStats(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return R.ok(attendanceService.getStudentAttendanceStats(studentId, startDate, endDate));
    }

    @GetMapping("/stats/class/{classId}")
    @Operation(summary = "班级考勤统计", description = "按学员分组统计数据")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<List<Map<String, Object>>> classStats(
            @PathVariable Long classId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return R.ok(attendanceService.getClassAttendanceStats(classId, startDate, endDate));
    }

}
