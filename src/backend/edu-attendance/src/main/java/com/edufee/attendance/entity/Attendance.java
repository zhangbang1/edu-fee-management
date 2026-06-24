package com.edufee.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤实体
 * 对应数据库 edu_attendance 表，记录学员每次上课的出勤情况
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_attendance")
public class Attendance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 排课ID */
    private Long scheduleId;

    /** 学员ID */
    private Long studentId;

    /** 班级ID */
    private Long classId;

    /** 考勤日期 */
    private LocalDate attendanceDate;

    /** 考勤状态: PRESENT-出勤 LATE-迟到 ABSENT-缺勤 LEAVE-请假 EARLY_LEAVE-早退 */
    private String status;

    /** 到校时间 */
    private LocalDateTime arriveTime;

    /** 离校时间 */
    private LocalDateTime leaveTime;

    /** 备注 */
    private String remark;

}
