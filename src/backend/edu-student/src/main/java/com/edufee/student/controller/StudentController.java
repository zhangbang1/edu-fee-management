package com.edufee.student.controller;

import com.edufee.common.PageDTO;
import com.edufee.common.R;
import com.edufee.student.dto.StudentDTO;
import com.edufee.student.dto.StudentDTO.TransferRequest;
import com.edufee.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 学员管理控制器
 * 提供学员信息的增删改查、入学注册、转班、退学等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
@Tag(name = "学员管理", description = "学员信息CRUD、入学、转班、退学")
public class StudentController {

    @Resource
    private StudentService studentService;

    /**
     * 分页查询学员列表
     * 支持按姓名模糊搜索、按状态和校区筛选
     *
     * @param page     页码（默认1）
     * @param size     每页条数（默认10）
     * @param name     学员姓名（可选）
     * @param status   学习状态（可选）
     * @param campusId 校区ID（可选）
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询学员列表", description = "支持按姓名模糊搜索、按状态和校区筛选")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<PageDTO<StudentDTO.DetailResponse>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "学员姓名") @RequestParam(required = false) String name,
            @Parameter(description = "学习状态") @RequestParam(required = false) String status,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId) {
        PageDTO<StudentDTO.DetailResponse> result = studentService.listStudents(page, size, name, status, campusId);
        return R.ok(result);
    }

    /**
     * 获取学员详情
     *
     * @param id 学员ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取学员详情", description = "根据学员ID查询详细信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<StudentDTO.DetailResponse> getById(@Parameter(description = "学员ID") @PathVariable Long id) {
        StudentDTO.DetailResponse student = studentService.getStudentById(id);
        return R.ok(student);
    }

    /**
     * 创建新学员（入学注册）
     *
     * @param request 创建请求
     */
    @PostMapping
    @Operation(summary = "学员入学注册", description = "创建新学员并自动生成学员编号")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<StudentDTO.DetailResponse> create(@Valid @RequestBody StudentDTO.CreateRequest request) {
        StudentDTO.DetailResponse student = studentService.createStudent(request);
        return R.ok("学员注册成功", student);
    }

    /**
     * 更新学员信息
     *
     * @param request 更新请求
     */
    @PutMapping
    @Operation(summary = "更新学员信息", description = "修改学员基本资料")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<StudentDTO.DetailResponse> update(@Valid @RequestBody StudentDTO.UpdateRequest request) {
        StudentDTO.DetailResponse student = studentService.updateStudent(request);
        return R.ok("学员信息更新成功", student);
    }

    /**
     * 学员转班
     *
     * @param request 转班请求
     */
    @PostMapping("/transfer")
    @Operation(summary = "学员转班", description = "将学员从当前班级转移到目标班级")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Void> transfer(@Valid @RequestBody TransferRequest request) {
        studentService.transferStudent(request);
        return R.ok("学员转班成功");
    }

    /**
     * 学员退学
     *
     * @param id     学员ID
     * @param reason 退学原因
     */
    @PostMapping("/{id}/withdraw")
    @Operation(summary = "学员退学", description = "为学员办理退学手续")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Void> withdraw(
            @Parameter(description = "学员ID") @PathVariable Long id,
            @Parameter(description = "退学原因") @RequestParam String reason) {
        studentService.withdrawStudent(id, reason);
        return R.ok("学员退学办理成功");
    }

}
