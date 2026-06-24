package com.edufee.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.common.PageDTO;
import com.edufee.course.entity.Course;

import java.util.List;

/**
 * 课程管理服务接口
 */
public interface CourseService {

    /**
     * 分页查询课程列表
     */
    PageDTO<Course> listCourses(long page, long size, String name, String category, Integer status);

    /**
     * 根据ID获取课程详情
     */
    Course getCourseById(Long id);

    /**
     * 创建新课程
     */
    Course createCourse(Course course);

    /**
     * 更新课程信息
     */
    Course updateCourse(Course course);

    /**
     * 课程上架/下架
     */
    void updateCourseStatus(Long id, Integer status);

    /**
     * 查询所有上架课程（供其他模块使用）
     */
    List<Course> listActiveCourses();

}
