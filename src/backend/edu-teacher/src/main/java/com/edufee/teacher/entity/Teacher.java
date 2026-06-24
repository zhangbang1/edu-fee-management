package com.edufee.teacher.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 教师实体
 * 对应数据库 edu_teacher 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_teacher")
public class Teacher extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 教师编号（唯一） */
    private String teacherNo;

    /** 教师姓名 */
    private String name;

    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 身份证号 */
    private String idCard;

    /** 学历 */
    private String education;

    /** 专业 */
    private String major;

    /** 入职日期 */
    private LocalDate hireDate;

    /** 状态: 0-离职 1-在职 2-休假 */
    private Integer status;

    /** 教学特长 */
    private String specialty;

    /** 备注 */
    private String remark;

}
