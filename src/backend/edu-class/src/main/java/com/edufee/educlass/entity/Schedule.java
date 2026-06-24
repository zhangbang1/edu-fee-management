package com.edufee.educlass.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排课实体
 * 对应数据库 edu_schedule 表，记录每节课的具体排课信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_schedule")
public class Schedule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 班级ID */
    private Long classId;

    /** 课程ID */
    private Long courseId;

    /** 教师ID */
    private Long teacherId;

    /** 校区ID */
    private Long campusId;

    /** 教室/场地 */
    private String classroom;

    /** 上课日期 */
    private LocalDate scheduleDate;

    /** 上课开始时间 */
    private LocalTime startTime;

    /** 上课结束时间 */
    private LocalTime endTime;

    /** 星期几(1-7) */
    private Integer weekDay;

    /** 课次类型: REGULAR-常规课 MAKEUP-补课 TRIAL-试听课 */
    private String lessonType;

    /** 状态: SCHEDULED-已排课 IN_PROGRESS-进行中 FINISHED-已完成 CANCELED-已取消 */
    private String status;

    /** 备注 */
    private String remark;

}
