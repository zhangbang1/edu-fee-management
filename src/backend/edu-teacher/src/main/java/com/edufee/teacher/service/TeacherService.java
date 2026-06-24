package com.edufee.teacher.service;

import com.edufee.common.PageDTO;
import com.edufee.teacher.entity.Teacher;

import java.util.List;

/**
 * 教师管理服务接口
 */
public interface TeacherService {

    /** 分页查询教师列表 */
    PageDTO<Teacher> listTeachers(long page, long size, String name, Integer status);

    /** 获取教师详情 */
    Teacher getTeacherById(Long id);

    /** 创建教师 */
    Teacher createTeacher(Teacher teacher);

    /** 更新教师信息 */
    Teacher updateTeacher(Teacher teacher);

    /** 更新教师状态（在职/离职/休假） */
    void updateTeacherStatus(Long id, Integer status);

    /** 查询所有在职教师（供班级、排课等模块选择） */
    List<Teacher> listActiveTeachers();

}
