package com.edufee.teacher.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.common.BusinessException;
import com.edufee.common.PageDTO;
import com.edufee.teacher.entity.Teacher;
import com.edufee.teacher.mapper.TeacherMapper;
import com.edufee.teacher.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 教师管理服务实现类
 */
@Slf4j
@Service
public class TeacherServiceImpl implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public PageDTO<Teacher> listTeachers(long page, long size, String name, Integer status) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Teacher::getName, name);
        }
        if (status != null) {
            wrapper.eq(Teacher::getStatus, status);
        }
        wrapper.orderByDesc(Teacher::getCreateTime);
        Page<Teacher> teacherPage = teacherMapper.selectPage(new Page<>(page, size), wrapper);
        return PageDTO.of(teacherPage);
    }

    @Override
    public Teacher getTeacherById(Long id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw BusinessException.notFound("教师不存在，ID: " + id);
        }
        return teacher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher createTeacher(Teacher teacher) {
        // 自动生成教师编号: TCH + 日期 + 4位随机数
        String datePart = LocalDate.now().format(DATE_FMT);
        teacher.setTeacherNo("TCH" + datePart + RandomUtil.randomNumbers(4));

        if (teacher.getStatus() == null) {
            teacher.setStatus(1); // 默认在职
        }

        teacherMapper.insert(teacher);
        log.info("教师创建成功: {} - {} (编号: {})", teacher.getId(), teacher.getName(), teacher.getTeacherNo());
        return teacher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher updateTeacher(Teacher teacher) {
        Teacher existing = getTeacherById(teacher.getId());
        teacher.setTeacherNo(existing.getTeacherNo());
        teacher.setCreateTime(existing.getCreateTime());
        teacher.setCreateBy(existing.getCreateBy());
        teacherMapper.updateById(teacher);
        log.info("教师信息更新成功: {} - {}", teacher.getId(), teacher.getName());
        return teacher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeacherStatus(Long id, Integer status) {
        if (status < 0 || status > 2) {
            throw BusinessException.badRequest("状态值无效，只能为0(离职)、1(在职)或2(休假)");
        }
        Teacher teacher = getTeacherById(id);
        teacher.setStatus(status);
        teacherMapper.updateById(teacher);
        log.info("教师状态更新: {} - {} -> {}", id, teacher.getName(), status == 1 ? "在职" : status == 0 ? "离职" : "休假");
    }

    @Override
    public List<Teacher> listActiveTeachers() {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getStatus, 1)
               .orderByAsc(Teacher::getName);
        return teacherMapper.selectList(wrapper);
    }

}
