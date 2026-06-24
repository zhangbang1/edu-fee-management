package com.edufee.educlass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.common.PageDTO;
import com.edufee.educlass.entity.EduClass;
import com.edufee.educlass.entity.Schedule;

import java.util.List;

/**
 * 班级管理服务接口
 * 定义班级CRUD和排课管理方法
 */
public interface ClassService {

    // ========== 班级管理 ==========

    /** 分页查询班级列表 */
    PageDTO<EduClass> listClasses(long page, long size, String name, String status, Long campusId);

    /** 获取班级详情 */
    EduClass getClassById(Long id);

    /** 创建班级 */
    EduClass createClass(EduClass eduClass);

    /** 更新班级信息 */
    EduClass updateClass(EduClass eduClass);

    /** 班级开课（状态从PENDING变更为IN_PROGRESS） */
    void startClass(Long id);

    /** 班级结课（状态变更为FINISHED） */
    void finishClass(Long id);

    // ========== 排课管理 ==========

    /** 查询班级的排课列表 */
    List<Schedule> listSchedules(Long classId, String dateRange);

    /** 创建排课记录（含时间冲突检测） */
    Schedule createSchedule(Schedule schedule);

    /** 取消排课 */
    void cancelSchedule(Long scheduleId);

    /** 检查排课冲突 */
    boolean checkScheduleConflict(Long teacherId, Long classroom, java.time.LocalDate date,
                                   java.time.LocalTime startTime, java.time.LocalTime endTime);

}
