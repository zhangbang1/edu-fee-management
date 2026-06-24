package com.edufee.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.attendance.entity.Attendance;
import com.edufee.attendance.mapper.AttendanceMapper;
import com.edufee.attendance.service.AttendanceService;
import com.edufee.common.BusinessException;
import com.edufee.common.Constants;
import com.edufee.common.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤管理服务实现类
 * 包含考勤记录查询、批量创建、状态更新、统计分析
 */
@Slf4j
@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Resource
    private AttendanceMapper attendanceMapper;

    /**
     * 分页查询考勤记录
     */
    @Override
    public PageDTO<Attendance> listAttendance(long page, long size, Long studentId, Long classId,
                                               String status, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Attendance::getStudentId, studentId);
        }
        if (classId != null) {
            wrapper.eq(Attendance::getClassId, classId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Attendance::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(Attendance::getAttendanceDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Attendance::getAttendanceDate, endDate);
        }
        wrapper.orderByDesc(Attendance::getAttendanceDate);

        Page<Attendance> attendancePage = attendanceMapper.selectPage(new Page<>(page, size), wrapper);
        return PageDTO.of(attendancePage);
    }

    /**
     * 根据排课ID查询考勤
     */
    @Override
    public List<Attendance> listByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getScheduleId, scheduleId)
               .orderByAsc(Attendance::getStudentId);
        return attendanceMapper.selectList(wrapper);
    }

    /**
     * 批量创建考勤记录
     * 根据排课ID和学员列表，为每个学员创建初始考勤记录（默认PRESENT）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Attendance> batchCreateAttendance(Long scheduleId, List<Long> studentIds) {
        // 检查是否已存在考勤记录（避免重复创建）
        LambdaQueryWrapper<Attendance> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Attendance::getScheduleId, scheduleId);
        if (attendanceMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException("该排课已生成考勤记录，无法重复创建");
        }

        List<Attendance> attendanceList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Long studentId : studentIds) {
            Attendance attendance = new Attendance();
            attendance.setScheduleId(scheduleId);
            attendance.setStudentId(studentId);
            attendance.setAttendanceDate(today);
            attendance.setStatus(Constants.ATTENDANCE_PRESENT); // 默认出勤
            attendanceList.add(attendance);
        }

        // 批量插入
        for (Attendance attendance : attendanceList) {
            attendanceMapper.insert(attendance);
        }

        log.info("批量创建考勤记录: scheduleId={}, 学员数={}", scheduleId, studentIds.size());
        return attendanceList;
    }

    /**
     * 更新考勤状态（签到/请假/迟到等）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAttendanceStatus(Long attendanceId, String status, String remark) {
        Attendance attendance = attendanceMapper.selectById(attendanceId);
        if (attendance == null) {
            throw BusinessException.notFound("考勤记录不存在，ID: " + attendanceId);
        }

        // 验证状态值合法性
        Set<String> validStatuses = new HashSet<>(Arrays.asList(
                Constants.ATTENDANCE_PRESENT, Constants.ATTENDANCE_LATE,
                Constants.ATTENDANCE_ABSENT, Constants.ATTENDANCE_LEAVE,
                Constants.ATTENDANCE_EARLY_LEAVE
        ));
        if (!validStatuses.contains(status)) {
            throw BusinessException.badRequest("无效的考勤状态: " + status);
        }

        attendance.setStatus(status);
        attendance.setRemark(remark);

        // 根据状态设置到校时间
        if (Constants.ATTENDANCE_PRESENT.equals(status) || Constants.ATTENDANCE_LATE.equals(status)) {
            attendance.setArriveTime(LocalDateTime.now());
        }

        attendanceMapper.updateById(attendance);
        log.info("考勤状态更新: attendanceId={}, studentId={}, status={}", attendanceId, attendance.getStudentId(), status);
    }

    /**
     * 学员考勤统计
     * 统计指定时间段内的出勤、缺勤、迟到、请假次数
     */
    @Override
    public Map<String, Object> getStudentAttendanceStats(Long studentId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getStudentId, studentId)
               .ge(startDate != null, Attendance::getAttendanceDate, startDate)
               .le(endDate != null, Attendance::getAttendanceDate, endDate);

        List<Attendance> records = attendanceMapper.selectList(wrapper);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRecords", records.size());
        stats.put("presentCount", countByStatus(records, Constants.ATTENDANCE_PRESENT));
        stats.put("lateCount", countByStatus(records, Constants.ATTENDANCE_LATE));
        stats.put("absentCount", countByStatus(records, Constants.ATTENDANCE_ABSENT));
        stats.put("leaveCount", countByStatus(records, Constants.ATTENDANCE_LEAVE));
        stats.put("earlyLeaveCount", countByStatus(records, Constants.ATTENDANCE_EARLY_LEAVE));

        // 出勤率 = (出勤 + 迟到 + 早退) / 总记录数
        long presentTotal = (long) stats.get("presentCount") + (long) stats.get("lateCount") + (long) stats.get("earlyLeaveCount");
        double attendanceRate = records.isEmpty() ? 0.0 : (double) presentTotal / records.size() * 100;
        stats.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);

        return stats;
    }

    /**
     * 班级考勤统计
     * 按学员分组统计考勤数据
     */
    @Override
    public List<Map<String, Object>> getClassAttendanceStats(Long classId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getClassId, classId)
               .ge(startDate != null, Attendance::getAttendanceDate, startDate)
               .le(endDate != null, Attendance::getAttendanceDate, endDate);

        List<Attendance> records = attendanceMapper.selectList(wrapper);

        // 按学员ID分组统计
        Map<Long, List<Attendance>> groupedByStudent = records.stream()
                .collect(Collectors.groupingBy(Attendance::getStudentId));

        return groupedByStudent.entrySet().stream().map(entry -> {
            List<Attendance> studentRecords = entry.getValue();
            Map<String, Object> studentStats = new LinkedHashMap<>();
            studentStats.put("studentId", entry.getKey());
            studentStats.put("total", studentRecords.size());
            studentStats.put("present", countByStatus(studentRecords, Constants.ATTENDANCE_PRESENT));
            studentStats.put("absent", countByStatus(studentRecords, Constants.ATTENDANCE_ABSENT));
            return studentStats;
        }).collect(Collectors.toList());
    }

    /**
     * 统计指定状态的记录数
     */
    private long countByStatus(List<Attendance> records, String status) {
        return records.stream().filter(r -> status.equals(r.getStatus())).count();
    }

}
