package com.edufee.educlass.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.common.BusinessException;
import com.edufee.common.Constants;
import com.edufee.common.PageDTO;
import com.edufee.educlass.entity.EduClass;
import com.edufee.educlass.entity.Schedule;
import com.edufee.educlass.mapper.ClassMapper;
import com.edufee.educlass.mapper.ScheduleMapper;
import com.edufee.educlass.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 班级管理服务实现类
 * 包含班级CRUD、开课/结课、排课管理及冲突检测
 */
@Slf4j
@Service
public class ClassServiceImpl implements ClassService {

    @Resource
    private ClassMapper classMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ======================== 班级管理 ========================

    /**
     * 分页查询班级列表
     */
    @Override
    public PageDTO<EduClass> listClasses(long page, long size, String name, String status, Long campusId) {
        LambdaQueryWrapper<EduClass> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(EduClass::getName, name);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(EduClass::getStatus, status);
        }
        if (campusId != null) {
            wrapper.eq(EduClass::getCampusId, campusId);
        }
        wrapper.orderByDesc(EduClass::getCreateTime);

        Page<EduClass> classPage = classMapper.selectPage(new Page<>(page, size), wrapper);
        return PageDTO.of(classPage);
    }

    /**
     * 获取班级详情
     */
    @Override
    public EduClass getClassById(Long id) {
        EduClass eduClass = classMapper.selectById(id);
        if (eduClass == null) {
            throw BusinessException.notFound("班级不存在，ID: " + id);
        }
        return eduClass;
    }

    /**
     * 创建班级
     * 自动生成班级编号，默认状态为PENDING
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduClass createClass(EduClass eduClass) {
        // 生成班级编号: CLA + 日期 + 4位随机数
        String datePart = LocalDate.now().format(DATE_FMT);
        String randomPart = RandomUtil.randomNumbers(4);
        eduClass.setClassNo("CLA" + datePart + randomPart);

        // 默认状态：待开课
        if (!StringUtils.hasText(eduClass.getStatus())) {
            eduClass.setStatus(Constants.CLASS_STATUS_PENDING);
        }

        // 初始化当前学员数为0
        if (eduClass.getCurrentStudents() == null) {
            eduClass.setCurrentStudents(0);
        }

        classMapper.insert(eduClass);
        log.info("班级创建成功: {} - {} (编号: {})", eduClass.getId(), eduClass.getName(), eduClass.getClassNo());
        return eduClass;
    }

    /**
     * 更新班级信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduClass updateClass(EduClass eduClass) {
        EduClass existing = getClassById(eduClass.getId());

        // 保留不可修改字段
        eduClass.setClassNo(existing.getClassNo());
        eduClass.setCreateTime(existing.getCreateTime());
        eduClass.setCreateBy(existing.getCreateBy());

        // 学员数不允许直接修改（由转班、入学流程控制）
        eduClass.setCurrentStudents(existing.getCurrentStudents());

        classMapper.updateById(eduClass);
        log.info("班级信息更新成功: {} - {}", eduClass.getId(), eduClass.getName());
        return eduClass;
    }

    /**
     * 班级开课
     * 从PENDING状态变更为IN_PROGRESS
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startClass(Long id) {
        EduClass eduClass = getClassById(id);

        if (!Constants.CLASS_STATUS_PENDING.equals(eduClass.getStatus())) {
            throw new BusinessException("只有待开课状态的班级才能开课，当前状态: " + eduClass.getStatus());
        }

        eduClass.setStatus(Constants.CLASS_STATUS_IN_PROGRESS);
        classMapper.updateById(eduClass);
        log.info("班级开课成功: {} - {}", id, eduClass.getName());
    }

    /**
     * 班级结课
     * 从IN_PROGRESS状态变更为FINISHED
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishClass(Long id) {
        EduClass eduClass = getClassById(id);

        if (!Constants.CLASS_STATUS_IN_PROGRESS.equals(eduClass.getStatus())) {
            throw new BusinessException("只有进行中状态的班级才能结课，当前状态: " + eduClass.getStatus());
        }

        eduClass.setStatus(Constants.CLASS_STATUS_FINISHED);
        eduClass.setEndDate(LocalDate.now());
        classMapper.updateById(eduClass);
        log.info("班级结课成功: {} - {}", id, eduClass.getName());
    }

    // ======================== 排课管理 ========================

    /**
     * 查询班级排课列表
     */
    @Override
    public List<Schedule> listSchedules(Long classId, String dateRange) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getClassId, classId);

        // 按日期范围筛选（格式: 2025-01-01,2025-12-31）
        if (StringUtils.hasText(dateRange) && dateRange.contains(",")) {
            String[] dates = dateRange.split(",");
            wrapper.ge(Schedule::getScheduleDate, LocalDate.parse(dates[0]));
            wrapper.le(Schedule::getScheduleDate, LocalDate.parse(dates[1]));
        }

        wrapper.orderByAsc(Schedule::getScheduleDate)
               .orderByAsc(Schedule::getStartTime);
        return scheduleMapper.selectList(wrapper);
    }

    /**
     * 创建排课记录（含时间冲突检测）
     * 检查三方面冲突：
     * 1. 同一教师在同一时间段是否已有排课
     * 2. 同一教室在同一时间段是否已被占用
     * 3. 同一班级在同一时间段是否已有排课
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Schedule createSchedule(Schedule schedule) {
        // 验证排课时间合理性
        if (schedule.getEndTime().isBefore(schedule.getStartTime())
                || schedule.getEndTime().equals(schedule.getStartTime())) {
            throw BusinessException.badRequest("排课结束时间必须大于开始时间");
        }

        // 执行冲突检测
        if (checkScheduleConflict(schedule.getTeacherId(), schedule.getClassroom(),
                schedule.getScheduleDate(), schedule.getStartTime(), schedule.getEndTime())) {
            throw BusinessException.badRequest("排课时间冲突：该教师或教室在指定时间段已被占用");
        }

        // 检查同一班级时间冲突
        if (checkClassScheduleConflict(schedule.getClassId(), schedule.getScheduleDate(),
                schedule.getStartTime(), schedule.getEndTime())) {
            throw BusinessException.badRequest("排课时间冲突：该班级在指定时间段已有排课");
        }

        // 设置默认值
        if (!StringUtils.hasText(schedule.getStatus())) {
            schedule.setStatus(Constants.SCHEDULE_STATUS_SCHEDULED);
        }
        if (!StringUtils.hasText(schedule.getLessonType())) {
            schedule.setLessonType("REGULAR");
        }

        // 计算星期几
        schedule.setWeekDay(schedule.getScheduleDate().getDayOfWeek().getValue());

        scheduleMapper.insert(schedule);
        log.info("排课创建成功: scheduleId={}, classId={}, teacherId={}, date={}, {} - {}",
                schedule.getId(), schedule.getClassId(), schedule.getTeacherId(),
                schedule.getScheduleDate(), schedule.getStartTime(), schedule.getEndTime());

        return schedule;
    }

    /**
     * 取消排课
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSchedule(Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw BusinessException.notFound("排课记录不存在，ID: " + scheduleId);
        }

        if (Constants.SCHEDULE_STATUS_FINISHED.equals(schedule.getStatus())) {
            throw new BusinessException("已完成的排课不能取消");
        }

        schedule.setStatus(Constants.SCHEDULE_STATUS_CANCELED);
        scheduleMapper.updateById(schedule);
        log.info("排课已取消: scheduleId={}", scheduleId);
    }

    /**
     * 检查排课冲突
     * 判断是否有其他排课记录与该时间段重叠
     *
     * @return true-存在冲突 false-无冲突
     */
    @Override
    public boolean checkScheduleConflict(Long teacherId, String classroom,
                                         LocalDate date, LocalTime startTime, LocalTime endTime) {
        // 检查教师冲突：同一教师同一天的排课是否有时间重叠
        LambdaQueryWrapper<Schedule> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(Schedule::getTeacherId, teacherId)
                .eq(Schedule::getScheduleDate, date)
                .ne(Schedule::getStatus, Constants.SCHEDULE_STATUS_CANCELED)
                .and(w -> w
                        // 新排课开始时间在已有排课时间段内
                        .or(w2 -> w2.le(Schedule::getStartTime, startTime).ge(Schedule::getEndTime, startTime))
                        // 新排课结束时间在已有排课时间段内
                        .or(w2 -> w2.le(Schedule::getStartTime, endTime).ge(Schedule::getEndTime, endTime))
                        // 新排课时间段完全包含已有排课时间段
                        .or(w2 -> w2.ge(Schedule::getStartTime, startTime).le(Schedule::getEndTime, endTime))
                );

        if (scheduleMapper.selectCount(teacherWrapper) > 0) {
            log.warn("教师排课冲突: teacherId={}, date={}, time={}-{}", teacherId, date, startTime, endTime);
            return true;
        }

        // 检查教室冲突
        LambdaQueryWrapper<Schedule> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Schedule::getClassroom, classroom)
                .eq(Schedule::getScheduleDate, date)
                .ne(Schedule::getStatus, Constants.SCHEDULE_STATUS_CANCELED)
                .and(w -> w
                        .or(w2 -> w2.le(Schedule::getStartTime, startTime).ge(Schedule::getEndTime, startTime))
                        .or(w2 -> w2.le(Schedule::getStartTime, endTime).ge(Schedule::getEndTime, endTime))
                        .or(w2 -> w2.ge(Schedule::getStartTime, startTime).le(Schedule::getEndTime, endTime))
                );

        if (scheduleMapper.selectCount(roomWrapper) > 0) {
            log.warn("教室排课冲突: classroom={}, date={}, time={}-{}", classroom, date, startTime, endTime);
            return true;
        }

        return false;
    }

    /**
     * 检查同一班级的排课时间冲突
     */
    private boolean checkClassScheduleConflict(Long classId, LocalDate date,
                                                LocalTime startTime, LocalTime endTime) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getClassId, classId)
                .eq(Schedule::getScheduleDate, date)
                .ne(Schedule::getStatus, Constants.SCHEDULE_STATUS_CANCELED)
                .and(w -> w
                        .or(w2 -> w2.le(Schedule::getStartTime, startTime).ge(Schedule::getEndTime, startTime))
                        .or(w2 -> w2.le(Schedule::getStartTime, endTime).ge(Schedule::getEndTime, endTime))
                        .or(w2 -> w2.ge(Schedule::getStartTime, startTime).le(Schedule::getEndTime, endTime))
                );

        return scheduleMapper.selectCount(wrapper) > 0;
    }

}
