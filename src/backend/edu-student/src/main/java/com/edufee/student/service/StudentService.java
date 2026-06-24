package com.edufee.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.common.PageDTO;
import com.edufee.student.dto.StudentDTO.CreateRequest;
import com.edufee.student.dto.StudentDTO.DetailResponse;
import com.edufee.student.dto.StudentDTO.TransferRequest;
import com.edufee.student.dto.StudentDTO.UpdateRequest;
import com.edufee.student.entity.Student;

/**
 * 学员管理服务接口
 * 定义学员CRUD、入学注册、转班、退学等核心业务方法
 */
public interface StudentService {

    /**
     * 分页查询学员列表
     *
     * @param page    当前页码
     * @param size    每页条数
     * @param name    学员姓名（模糊查询，可选）
     * @param status  学习状态（可选）
     * @param campusId 校区ID（可选）
     * @return 分页结果
     */
    PageDTO<DetailResponse> listStudents(long page, long size, String name, String status, Long campusId);

    /**
     * 根据ID获取学员详情
     *
     * @param id 学员ID
     * @return 学员详情DTO
     */
    DetailResponse getStudentById(Long id);

    /**
     * 创建新学员（入学注册）
     * 自动生成学员编号，设置初始状态为STUDYING
     *
     * @param request 创建请求
     * @return 学员详情DTO
     */
    DetailResponse createStudent(CreateRequest request);

    /**
     * 更新学员基本信息
     *
     * @param request 更新请求
     * @return 学员详情DTO
     */
    DetailResponse updateStudent(UpdateRequest request);

    /**
     * 学员转班
     * 将学员从当前班级转到目标班级，更新classId和status
     *
     * @param request 转班请求
     */
    void transferStudent(TransferRequest request);

    /**
     * 学员退学
     * 将学员状态设置为WITHDRAWN，保留历史数据
     *
     * @param id     学员ID
     * @param reason 退学原因
     */
    void withdrawStudent(Long id, String reason);

    /**
     * 根据ID获取学员实体（供其他模块内部调用）
     *
     * @param id 学员ID
     * @return 学员实体
     */
    Student getStudentEntityById(Long id);

}
