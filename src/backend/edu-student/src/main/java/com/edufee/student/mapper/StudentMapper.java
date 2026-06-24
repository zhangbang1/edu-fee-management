package com.edufee.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edufee.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学员 Mapper 接口
 * 继承MyBatis-Plus BaseMapper，提供学员数据访问能力
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}
