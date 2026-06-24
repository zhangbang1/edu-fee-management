package com.edufee.course.controller;

import com.edufee.common.PageDTO;
import com.edufee.common.R;
import com.edufee.course.entity.Course;
import com.edufee.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 课程管理控制器
 * 提供课程信息的增删改查、上下架管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/course")
@Tag(name = "课程管理", description = "课程信息CRUD、上下架管理")
public class CourseController {

    @Resource
    private CourseService courseService;

    /**
     * 分页查询课程列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询课程列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<PageDTO<Course>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        return R.ok(courseService.listCourses(page, size, name, category, status));
    }

    /**
     * 查询所有上架课程（下拉选择）
     */
    @GetMapping("/active")
    @Operation(summary = "查询所有上架课程", description = "供班级、收费等模块下拉选择使用")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<List<Course>> listActive() {
        return R.ok(courseService.listActiveCourses());
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<Course> getById(@PathVariable Long id) {
        return R.ok(courseService.getCourseById(id));
    }

    /**
     * 创建新课程
     */
    @PostMapping
    @Operation(summary = "创建新课程")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Course> create(@Valid @RequestBody Course course) {
        return R.ok("课程创建成功", courseService.createCourse(course));
    }

    /**
     * 更新课程信息
     */
    @PutMapping
    @Operation(summary = "更新课程信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Course> update(@Valid @RequestBody Course course) {
        return R.ok("课程更新成功", courseService.updateCourse(course));
    }

    /**
     * 课程上架/下架
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "课程上架/下架")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseService.updateCourseStatus(id, status);
        return R.ok(status == 1 ? "课程已上架" : "课程已下架");
    }

}
