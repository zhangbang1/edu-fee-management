package com.edufee.student.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 学员数据传输对象集合
 * 包含创建请求、更新请求、详情响应内部类
 */
public class StudentDTO {

    /**
     * 创建学员请求DTO
     */
    @Data
    public static class CreateRequest {

        /** 学员姓名，不能为空 */
        @NotBlank(message = "学员姓名不能为空")
        private String name;

        /** 性别: 1-男 2-女 */
        @NotNull(message = "性别不能为空")
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

        /** 来源渠道 */
        private String source;

        /** 所属校区ID */
        @NotNull(message = "所属校区不能为空")
        private Long campusId;

        /** 班级ID（可选，入学时可不指定班级） */
        private Long classId;

        /** 入学日期 */
        private LocalDate enrollDate;

        /** 备注 */
        private String remark;
    }

    /**
     * 更新学员请求DTO
     */
    @Data
    public static class UpdateRequest {

        /** 学员编号，不能为空 */
        @NotNull(message = "学员ID不能为空")
        private Long id;

        /** 学员姓名 */
        private String name;

        /** 性别 */
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

        /** 来源渠道 */
        private String source;

        /** 所属校区ID */
        private Long campusId;

        /** 备注 */
        private String remark;
    }

    /**
     * 转班请求DTO
     */
    @Data
    public static class TransferRequest {

        /** 学员ID */
        @NotNull(message = "学员ID不能为空")
        private Long studentId;

        /** 目标班级ID */
        @NotNull(message = "目标班级不能为空")
        private Long targetClassId;

        /** 转班原因 */
        @NotBlank(message = "转班原因不能为空")
        private String reason;
    }

    /**
     * 学员详情响应DTO
     */
    @Data
    public static class DetailResponse {

        /** 学员ID */
        private Long id;

        /** 学员编号 */
        private String studentNo;

        /** 学员姓名 */
        private String name;

        /** 性别 */
        private Integer gender;

        /** 性别文本 */
        private String genderText;

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

        /** 来源渠道 */
        private String source;

        /** 学习状态 */
        private String status;

        /** 状态文本 */
        private String statusText;

        /** 所属校区ID */
        private Long campusId;

        /** 当前班级ID */
        private Long classId;

        /** 入学日期 */
        private LocalDate enrollDate;

        /** 备注 */
        private String remark;

        /** 创建时间 */
        private String createTime;

        /** 更新时间 */
        private String updateTime;
    }

}
