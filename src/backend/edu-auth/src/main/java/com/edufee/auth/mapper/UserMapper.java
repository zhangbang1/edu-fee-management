package com.edufee.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edufee.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper 接口
 * 继承MyBatis-Plus BaseMapper，提供基础CRUD能力
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（MyBatis-Plus会自动生成，此处显式声明加强可读性）
     *
     * @param username 用户名
     * @return 用户实体
     */
    default User selectByUsername(String username) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

}
