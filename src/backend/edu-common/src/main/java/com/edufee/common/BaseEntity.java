package com.edufee.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有数据库实体继承此类，统一管理公共字段
 * 使用MyBatis-Plus的自动填充功能
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，使用数据库自增策略 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 创建时间，插入时自动填充 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人ID，插入时自动填充 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人ID，插入和更新时自动填充 */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标识: 0-未删除 1-已删除 */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

}
