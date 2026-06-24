package com.edufee.common;

import lombok.Getter;

/**
 * 业务异常类
 * 用于在Service层抛出业务逻辑相关的异常，由GlobalExceptionHandler统一捕获处理
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    /** 错误消息 */
    private final String message;

    /**
     * 使用默认错误码500创建业务异常
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 使用自定义错误码创建业务异常
     *
     * @param code    错误码
     * @param message 异常消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 快速创建404资源不存在异常
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 快速创建400参数错误异常
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

}
