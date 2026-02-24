package org.example.shopdemo.common;

import lombok.Data;

/**
 * 统一响应结果类
 * 用于封装API接口的返回结果
 */
@Data
public class Result<T> {
    /** 响应状态码 */
    private Integer code;
    /** 响应消息 */
    private String message;
    /** 响应数据 */
    private T data;

    /**
     * 成功响应（带数据）
     * @param data 响应数据
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（带消息和数据）
     * @param message 响应消息
     * @param data 响应数据
     * @return Result对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（带消息）
     * @param message 响应消息
     * @return Result对象
     */
    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /**
     * 失败响应（带消息）
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（带状态码和消息）
     * @param code 状态码
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}

