package com.edufee.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 所有Controller接口统一使用此类返回给前端
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应状态码 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 响应时间戳 */
    private long timestamp;

    // ============ 成功响应 ============

    /**
     * 操作成功，无返回数据
     */
    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null, System.currentTimeMillis());
    }

    /**
     * 操作成功，返回数据
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data, System.currentTimeMillis());
    }

    /**
     * 操作成功，自定义消息和数据
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data, System.currentTimeMillis());
    }

    // ============ 失败响应 ============

    /**
     * 操作失败，自定义状态码和消息
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null, System.currentTimeMillis());
    }

    /**
     * 操作失败，默认500错误
     */
    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null, System.currentTimeMillis());
    }

    /**
     * 参数校验失败
     */
    public static <T> R<T> badRequest(String message) {
        return new R<>(400, message, null, System.currentTimeMillis());
    }

    /**
     * 未授权
     */
    public static <T> R<T> unauthorized(String message) {
        return new R<>(401, message, null, System.currentTimeMillis());
    }

    /**
     * 无权限
     */
    public static <T> R<T> forbidden(String message) {
        return new R<>(403, message, null, System.currentTimeMillis());
    }

    /**
     * 资源不存在
     */
    public static <T> R<T> notFound(String message) {
        return new R<>(404, message, null, System.currentTimeMillis());
    }

}
