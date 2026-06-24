package com.edufee.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学员实体
 * 对应数据库 edu_student 表，记录学员基本信息和学习状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_student")
public class Student extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 学员编号（唯一） */
    private String studentNo;

    /** 学员姓名 */
    private String name;

    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 联系电话 */
    private String phone;

    /** 紧急联系人姓名 */
    private String contactName;

    /** 紧急联系人电话 */
    private String contactPhone;

    /** 与学员关系 */
    private String contactRelation;

    /** 就读学校 */
    private String school;

    /** 年级 */
    private String grade;

    /** 家庭地址 */
    private String address;

    /** 来源渠道: 地推/转介绍/线上广告等 */
    private String source;

    /** 学习状态: STUDYING-在读 SUSPENDED-停课 TRANSFERRED-转班 WITHDRAWN-退学 GRADUATED-毕业 */
    private String status;

    /** 所属校区ID */
    private Long campusId;

    /** 当前班级ID */
    private Long classId;

    /** 入学日期 */
    private LocalDate enrollDate;

    /** 备注 */
    private String remark;

}
