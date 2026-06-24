package com.edufee.educlass.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 班级实体
 * 对应数据库 edu_class 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_class")
public class EduClass extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 班级编号（唯一） */
    private String classNo;

    /** 班级名称 */
    private String name;

    /** 关联课程ID */
    private Long courseId;

    /** 授课教师ID */
    private Long teacherId;

    /** 所属校区ID */
    private Long campusId;

    /** 最大学员数 */
    private Integer maxStudents;

    /** 当前学员数 */
    private Integer currentStudents;

    /** 教室/场地 */
    private String classroom;

    /** 开课日期 */
    private LocalDate startDate;

    /** 结课日期 */
    private LocalDate endDate;

    /** 状态: PENDING-待开课 IN_PROGRESS-进行中 FINISHED-已结课 CANCELED-已取消 */
    private String status;

    /** 备注 */
    private String remark;

}
