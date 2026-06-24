package com.edufee.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询响应DTO
 * 封装分页数据，统一分页返回格式
 *
 * @param <T> 分页数据项类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private long current;

    /** 每页条数 */
    private long size;

    /** 总记录数 */
    private long total;

    /** 总页数 */
    private long pages;

    /** 当前页数据列表 */
    private List<T> records;

    /**
     * 将MyBatis-Plus的分页对象转换为统一的PageDTO
     *
     * @param page MyBatis-Plus分页对象
     * @param <T>  数据类型
     * @return 统一分页DTO
     */
    public static <T> PageDTO<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        PageDTO<T> dto = new PageDTO<>();
        dto.setCurrent(page.getCurrent());
        dto.setSize(page.getSize());
        dto.setTotal(page.getTotal());
        dto.setPages(page.getPages());
        dto.setRecords(page.getRecords() != null ? page.getRecords() : Collections.emptyList());
        return dto;
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageDTO<T> empty(long current, long size) {
        return new PageDTO<>(current, size, 0, 0, Collections.emptyList());
    }

}
