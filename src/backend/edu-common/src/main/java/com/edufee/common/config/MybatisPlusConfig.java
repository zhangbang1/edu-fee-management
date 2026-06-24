package com.edufee.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件、乐观锁插件、防全表更新删除插件以及自动填充
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * - 分页插件(PaginationInnerInterceptor)
     * - 乐观锁插件(OptimisticLockerInnerInterceptor)
     * - 防全表更新删除拦截器(BlockAttackInnerInterceptor)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件，指定数据库类型为H2（MySQL兼容模式）
        PaginationInnerInterceptor paginationInner = new PaginationInnerInterceptor(DbType.H2);
        // 设置最大单页限制为100条
        paginationInner.setMaxLimit(100L);
        // 溢出总页数后处理为第一页
        paginationInner.setOverflow(true);
        interceptor.addInnerInterceptor(paginationInner);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防全表更新删除拦截器
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * MyBatis-Plus 自动填充处理器
     * 自动填充 createTime/updateTime/createBy/updateBy 字段
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {

            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                // TODO: 从SecurityContext中获取当前登录用户ID
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "createBy", Long.class, getCurrentUserId());
                this.strictInsertFill(metaObject, "updateBy", Long.class, getCurrentUserId());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
            }

            /**
             * 获取当前登录用户ID
             * 从SecurityContextHolder中获取，如果未登录则返回null
             */
            private Long getCurrentUserId() {
                try {
                    // 尝试从Spring Security上下文中获取
                    org.springframework.security.core.Authentication auth =
                            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.getPrincipal() instanceof com.edufee.auth.security.JwtUserDetails) {
                        return ((com.edufee.auth.security.JwtUserDetails) auth.getPrincipal()).getUserId();
                    }
                } catch (Exception ignored) {
                    // 忽略异常，返回null
                }
                return null;
            }
   