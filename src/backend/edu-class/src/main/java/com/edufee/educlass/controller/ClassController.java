package com.edufee.educlass.controller;

import com.edufee.common.PageDTO;
import com.edufee.common.R;
import com.edufee.educlass.entity.EduClass;
import com.edufee.educlass.entity.Schedule;
import com.edufee.educlass.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 班级管理控制器
 * 提供班级CRUD、开课/结课、排课管理等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/class")
@Tag(name = "班级管理", description = "班级CRUD、开课结课、排课管理")
public class ClassController {

    @Resource
    private ClassService classService;

    // ==================== 班级管理 ====================

    @GetMapping("/list")
    @Operation(summary = "分页查询班级列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<PageDTO<EduClass>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long campusId) {
        return R.ok(classService.listClasses(page, size, name, status, campusId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取班级详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'FINANCE')")
    public R<EduClass> getById(@PathVariable Long id) {
        return R.ok(classService.getClassById(id));
    }

    @PostMapping
    @Operation(summary = "创建班级")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<EduClass> create(@RequestBody EduClass eduClass) {
        return R.ok("班级创建成功", classService.createClass(eduClass));
    }

    @PutMapping
    @Operation(summary = "更新班级信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<EduClass> update(@RequestBody EduClass eduClass) {
        return R.ok("班级信息更新成功", classService.updateClass(eduClass));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "班级开课", description = "将班级状态从'待开课'变更为'进行中'")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Void> startClass(@PathVariable Long id) {
        classService.startClass(id);
        return R.ok("班级已开课");
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "班级结课", description = "将班级状态从'进行中'变更为'已结课'")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Void> finishClass(@PathVariable Long id) {
        classService.finishClass(id);
        return R.ok("班级已结课");
    }

    // ==================== 排课管理 ====================

    @GetMapping("/{classId}/schedules")
    @Operation(summary = "查询班级排课列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public R<List<Schedule>> listSchedules(
            @PathVariable Long classId,
            @RequestParam(required = false) String dateRange) {
        return R.ok(classService.listSchedules(classId, dateRange));
    }

    @PostMapping("/schedule")
    @Operation(summary = "创建排课", description = "创建排课记录，自动检测教师和教室时间冲突")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Schedule> createSchedule(@RequestBody Schedule schedule) {
        return R.ok("排课成功", classService.createSchedule(schedule));
    }

    @PostMapping("/schedule/{scheduleId}/cancel")
    @Operation(summary = "取消排课")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public R<Void> cancelSchedule(@PathVariable Long scheduleId) {
        classService.cancelSchedule(scheduleId);
        return R.ok("排课已取消");
    }

}
