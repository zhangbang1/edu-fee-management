package com.edufee.educlass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edufee.educlass.entity.EduClass;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级 Mapper 接口
 */
@Mapper
public interface ClassMapper extends BaseMapper<EduClass> {

}
