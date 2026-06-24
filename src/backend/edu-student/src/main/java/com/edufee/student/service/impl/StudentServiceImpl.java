package com.edufee.student.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edufee.common.BusinessException;
import com.edufee.common.Constants;
import com.edufee.common.PageDTO;
import com.edufee.student.dto.StudentDTO.CreateRequest;
import com.edufee.student.dto.StudentDTO.DetailResponse;
import com.edufee.student.dto.StudentDTO.TransferRequest;
import com.edufee.student.dto.StudentDTO.UpdateRequest;
import com.edufee.student.entity.Student;
import com.edufee.student.mapper.StudentMapper;
import com.edufee.student.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学员管理服务实现类
 * 包含学员CRUD、入学注册、转班、退学等完整业务逻辑
 */
@Slf4j
@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper studentMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 分页查询学员列表
     * 支持按姓名模糊搜索、按状态筛选、按校区筛选
     */
    @Override
    public PageDTO<DetailResponse> listStudents(long page, long size, String name, String status, Long campusId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        // 姓名模糊查询
        if (StringUtils.hasText(name)) {
            wrapper.like(Student::getName, name);
        }
        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(Student::getStatus, status);
        }
        // 校区筛选
        if (campusId != null) {
            wrapper.eq(Student::getCampusId, campusId);
        }
        // 按创建时间倒序
        wrapper.orderByDesc(Student::getCreateTime);

        Page<Student> studentPage = studentMapper.selectPage(new Page<>(page, size), wrapper);

        List<DetailResponse> records = studentPage.getRecords().stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());

        PageDTO<DetailResponse> result = PageDTO.of(studentPage);
        result.setRecords(records);
        return result;
    }

    /**
     * 根据ID获取学员详情
     */
    @Override
    public DetailResponse getStudentById(Long id) {
        Student student = getStudentEntityById(id);
        return convertToDetailResponse(student);
    }

    /**
     * 创建新学员（入学注册）
     * 1. 自动生成学员编号（格式: STU + yyyyMMdd + 4位随机数）
     * 2. 设置初始状态为STUDYING
     * 3. 设置默认入学日期为当天
     * 4. 保存学员信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetailResponse createStudent(CreateRequest request) {
        Student student = new Student();

        // 拷贝请求参数到实体
        BeanUtils.copyProperties(request, student);

        // 自动生成学员编号: STU + 日期 + 4位随机数
        String datePart = LocalDate.now().format(DATE_FMT);
        String randomPart = RandomUtil.randomNumbers(4);
        student.setStudentNo("STU" + datePart + randomPart);

        // 设置初始状态为在读
        student.setStatus(Constants.STUDENT_STATUS_STUDYING);

        // 如果未指定入学日期，默认为当天
        if (student.getEnrollDate() == null) {
            student.setEnrollDate(LocalDate.now());
        }

        studentMapper.insert(student);

        log.info("学员入学注册成功: {} - {} (编号: {})", student.getId(), student.getName(), student.getStudentNo());

        return convertToDetailResponse(student);
    }

    /**
     * 更新学员基本信息
     * 校验学员是否存在，然后更新字段（非空字段才更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetailResponse updateStudent(UpdateRequest request) {
        // 查询学员是否存在
        Student student = getStudentEntityById(request.getId());

        // 只更新传入的非空字段
        if (StringUtils.hasText(request.getName())) {
            student.setName(request.getName());
        }
        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }
        if (request.getBirthDate() != null) {
            student.setBirthDate(request.getBirthDate());
        }
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        if (request.getContactName() != null) {
            student.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            student.setContactPhone(request.getContactPhone());
        }
        if (request.getContactRelation() != null) {
            student.setContactRelation(request.getContactRelation());
        }
        if (request.getSchool() != null) {
            student.setSchool(request.getSchool());
        }
        if (request.getGrade() != null) {
            student.setGrade(request.getGrade());
        }
        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }
        if (request.getSource() != null) {
            student.setSource(request.getSource());
        }
        if (request.getCampusId() != null) {
            student.setCampusId(request.getCampusId());
        }
        if (request.getRemark() != null) {
            student.setRemark(request.getRemark());
        }

        studentMapper.updateById(student);

        log.info("学员信息更新成功: {} - {}", student.getId(), student.getName());

        return convertToDetailResponse(student);
    }

    /**
     * 学员转班
     * 1. 检查学员是否存在
     * 2. 检查学员是否处于可转班状态（在读或停课）
     * 3. 更新班级ID和状态为TRANSFERRED
     * 4. 实际业务中还需要处理目标班级容量检查、原班级人数更新等
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferStudent(TransferRequest request) {
        Student student = getStudentEntityById(request.getStudentId());

        // 检查学员状态是否允许转班
        String currentStatus = student.getStatus();
        if (Constants.STUDENT_STATUS_WITHDRAWN.equals(currentStatus)) {
            throw new BusinessException("已退学的学员无法转班");
        }
        if (Constants.STUDENT_STATUS_GRADUATED.equals(currentStatus)) {
            throw new BusinessException("已毕业的学员无法转班");
        }

        Long oldClassId = student.getClassId();

        // 更新班级和状态
        student.setClassId(request.getTargetClassId());
        student.setStatus(Constants.STUDENT_STATUS_TRANSFERRED);
        studentMapper.updateById(student);

        log.info("学员转班成功: 学员 {} 从班级 {} 转到班级 {}, 原因: {}",
                student.getName(), oldClassId, request.getTargetClassId(), request.getReason());

        // TODO: 发送转班通知
        // TODO: 更新原班级的current_students数量减1
        // TODO: 更新目标班级的current_students数量加1
    }

    /**
     * 学员退学
     * 将学员状态设置为WITHDRAWN（逻辑退学，保留数据）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawStudent(Long id, String reason) {
        Student student = getStudentEntityById(id);

        // 检查学员是否已退学
        if (Constants.STUDENT_STATUS_WITHDRAWN.equals(student.getStatus())) {
            throw new BusinessException("该学员已经退学");
        }

        student.setStatus(Constants.STUDENT_STATUS_WITHDRAWN);
        student.setRemark((student.getRemark() != null ? student.getRemark() + " " : "")
                + "[退学原因: " + reason + "]");
        studentMapper.updateById(student);

        log.info("学员退学处理完成: {} - {}, 原因: {}", id, student.getName(), reason);

        // TODO: 更新所属班级的current_students数量
        // TODO: 清算该学员未结费用
    }

    /**
     * 根据ID获取学员实体（供内部调用）
     * 学员不存在时抛出业务异常
     */
    @Override
    public Student getStudentEntityById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw BusinessException.notFound("学员不存在，ID: " + id);
        }
        return student;
    }

    /**
     * 将学员实体转换为详情响应DTO
     */
    private DetailResponse convertToDetailResponse(Student student) {
        DetailResponse response = new DetailResponse();
        BeanUtils.copyProperties(student, response);

        // 设置性别文本
        if (student.getGender() != null) {
            response.setGenderText(student.getGender() == 1 ? "男" : student.getGender() == 2 ? "女" : "未知");
        }

        // 设置状态文本
        response.setStatusText(getStatusText(student.getStatus()));

        // 格式化日期
        if (student.getCreateTime() != null) {
            response.setCreateTime(DateUtil.format(student.getCreateTime(), Constants.DATETIME_FORMAT));
        }
        if (student.getUpdateTime() != null) {
            response.setUpdateTime(DateUtil.format(student.getUpdateTime(), Constants.DATETIME_FORMAT));
        }

        return response;
    }

    /**
     * 获取状态的中文描述
     */
    private String getStatusText(String status) {
        switch (status) {
            case Constants.STUDENT_STATUS_STUDYING:
                return "在读";
            case Constants.STUDENT_STATUS_SUSPENDED:
                return "停课";
            case Constants.STUDENT_STATUS_TRANSFERRED:
                return "转班";
            case Constants.STUDENT_STATUS_WITHDRAWN:
                return "退学";
            case Constants.STUDENT_STATUS_GRADUATED:
                return "毕业";
            default:
                return "未知";
        }
    }

}
