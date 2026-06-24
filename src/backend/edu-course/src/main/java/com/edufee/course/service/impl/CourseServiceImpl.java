package com.edufee.course.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.common.BusinessException;
import com.edufee.common.PageDTO;
import com.edufee.course.entity.Course;
import com.edufee.course.mapper.CourseMapper;
import com.edufee.course.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 课程管理服务实现类
 * 包含课程CRUD、上下架管理等业务逻辑
 */
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 分页查询课程列表
     * 支持按名称模糊查询、分类筛选、状态筛选
     */
    @Override
    public PageDTO<Course> listCourses(long page, long size, String name, String category, Integer status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Course::getName, name);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Course::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = courseMapper.selectPage(new Page<>(page, size), wrapper);
        return PageDTO.of(coursePage);
    }

    /**
     * 根据ID获取课程详情
     */
    @Override
    public Course getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw BusinessException.notFound("课程不存在，ID: " + id);
        }
        return course;
    }

    /**
     * 创建新课程
     * 自动生成课程编号（格式: COU + yyyyMMdd + 4位随机数）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Course createCourse(Course course) {
        // 自动生成课程编号
        String datePart = LocalDate.now().format(DATE_FMT);
        String randomPart = RandomUtil.randomNumbers(4);
        course.setCourseNo("COU" + datePart + randomPart);

        // 默认上架状态
        if (course.getStatus() == null) {
            course.setStatus(1);
        }

        courseMapper.insert(course);
        log.info("课程创建成功: {} - {} (编号: {})", course.getId(), course.getName(), course.getCourseNo());
        return course;
    }

    /**
     * 更新课程信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Course updateCourse(Course course) {
        // 检查课程是否存在
        Course existing = courseMapper.selectById(course.getId());
        if (existing == null) {
            throw BusinessException.notFound("课程不存在，ID: " + course.getId());
        }

        // 保留不可修改的字段
        course.setCourseNo(existing.getCourseNo());
        course.setCreateTime(existing.getCreateTime());
        course.setCreateBy(existing.getCreateBy());

        courseMapper.updateById(course);
        log.info("课程信息更新成功: {} - {}", course.getId(), course.getName());
        return course;
    }

    /**
     * 课程上架/下架
     * 校验状态值合法性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw BusinessException.badRequest("状态值无效，只能为0(下架)或1(上架)");
        }

        Course course = getCourseById(id);
        course.setStatus(status);
        courseMapper.updateById(course);

        log.info("课程状态更新: {} - {} -> {}", id, course.getName(), status == 1 ? "上架" : "下架");
    }

    /**
     * 查询所有上架课程
     * 供班级、收费等模块下拉选择使用
     */
    @Override
    public List<Course> listActiveCourses() {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);
        wrapper.orderByAsc(Course::getCategory)
               .orderByAsc(Course::getName);
        return courseMapper.selectList(wrapper);
    }

}
