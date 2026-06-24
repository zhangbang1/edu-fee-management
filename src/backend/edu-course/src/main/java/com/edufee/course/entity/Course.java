package com.edufee.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 课程实体
 * 对应数据库 edu_course 表，记录培训机构开设的各类课程信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_course")
public class Course extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 课程编号（唯一） */
    private String courseNo;

    /** 课程名称 */
    private String name;

    /** 课程分类: 学科辅导/艺术培训/体育培训/语言培训等 */
    private String category;

    /** 适龄段: 3-6岁/6-12岁/12-18岁/成人 */
    private String ageGroup;

    /** 课程简介 */
    private String description;

    /** 总课时数 */
    private Integer totalHours;

    /** 收费模式: COURSE-按课程收费 HOUR-按课时收费 PERIOD-按期收费 */
    private String priceType;

    /** 单价（元） */
    private BigDecimal unitPrice;

    /** 封面图片URL */
    private String coverImage;

    /** 状态: 0-下架 1-上架 */
    private Integer status;

    /** 备注 */
    private String remark;

}
