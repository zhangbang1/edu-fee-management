package com.edufee.attendance.service;

import com.edufee.attendance.entity.Attendance;
import com.edufee.common.PageDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理服务接口
 */
public interface AttendanceService {

    /** 分页查询考勤记录 */
    PageDTO<Attendance> listAttendance(long page, long size, Long studentId, Long classId, String status,
                                        LocalDate startDate, LocalDate endDate);

    /** 根据排课ID查询考勤 */
    List<Attendance> listByScheduleId(Long scheduleId);

    /** 批量创建考勤记录（按排课自动生成） */
    List<Attendance> batchCreateAttendance(Long scheduleId, List<Long> studentIds);

    /** 更新考勤状态（签到/请假/迟到等） */
    void updateAttendanceStatus(Long attendanceId, String status, String remark);

    /** 学员考勤统计 */
    Map<String, Object> getStudentAttendanceStats(Long studentId, LocalDate startDate, LocalDate endDate);

    /** 班级考勤统计 */
    List<Map<String, Object>> getClassAttendanceStats(Long classId, LocalDate startDate, LocalDate endDate);

}
