package org.example.shopdemo.exception;

import org.example.shopdemo.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理各种异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理限流异常
     */
    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleRateLimitException(RateLimitException e) {
        return Result.error(429, e.getMessage());
    }

    /**
     * 处理权限拒绝异常
     */
    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handlePermissionDeniedException(PermissionDeniedException e) {
        return Result.error(403, e.getMessage());
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(500, "系统错误：" + e.getMessage());
    }
}
