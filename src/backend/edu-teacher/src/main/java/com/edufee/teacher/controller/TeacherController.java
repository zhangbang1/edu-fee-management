package com.edufee.teacher.controller;

import com.edufee.common.PageDTO;
import com.edufee.common.R;
import com.edufee.teacher.entity.Teacher;
import com.edufee.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 教师管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher")
@Tag(name = "教师管理", description = "教师信息CRUD、状态管理")
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    @GetMapping("/list")
    @Operation(summary = "分页查询教师列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'FINANCE')")
    public R<PageDTO<Teacher>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return R.ok(teacherService.listTeachers(page, size, name, status));
    }

    @GetMapping("/active")
    @Operation(summary = "查询所有在职教师")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'FINANCE', 'TEACHER')")
    public R<List<Teacher>> listActive() {
        return R.ok(teacherService.listActiveTeachers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取教师详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'FINANCE', 'TEACHER')")
    public R<Teacher> getById(@PathVariable Long id) {
        return R.ok(teacherService.getTeacherById(id));
    }

    @PostMapping
    @Operation(summary = "创建教师")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Teacher> create(@RequestBody Teacher teacher) {
        return R.ok("教师创建成功", teacherService.createTeacher(teacher));
    }

    @PutMapping
    @Operation(summary = "更新教师信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Teacher> update(@RequestBody Teacher teacher) {
        return R.ok("教师信息更新成功", teacherService.updateTeacher(teacher));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新教师状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        teacherService.updateTeacherStatus(id, status);
        return R.ok("教师状态更新成功");
    }

}
