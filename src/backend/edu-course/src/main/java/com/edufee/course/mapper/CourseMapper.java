package com.edufee.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edufee.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程 Mapper 接口
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

}
