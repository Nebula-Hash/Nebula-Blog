package com.nebula.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.nebula.exception.BusinessException;
import com.nebula.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * 全局异常处理器
 *
 * @author Nebula-Hash
 * @date 2026/2/25
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未登录异常
     *
     * @param e 未登录异常
     * @return 统一错误响应
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException e) {
        log.error("用户未登录: {}", e.getMessage());
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供登录令牌";
            case NotLoginException.INVALID_TOKEN -> "登录令牌无效";
            case NotLoginException.TOKEN_TIMEOUT -> "登录令牌已过期";
            case NotLoginException.BE_REPLACED -> "登录令牌已被顶下线";
            case NotLoginException.KICK_OUT -> "登录令牌已被踢下线";
            default -> "当前会话未登录";
        };
        return Result.error(401, message);
    }

    /**
     * 处理权限不足异常
     *
     * @param e 权限异常
     * @return 统一错误响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<String> handleNotPermissionException(NotPermissionException e) {
        log.error("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足");
    }

    /**
     * 处理角色不足异常
     *
     * @param e 角色异常
     * @return 统一错误响应
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<String> handleNotRoleException(NotRoleException e) {
        log.error("角色不足: {}", e.getMessage());
        return Result.error(403, "角色不足");
    }

    /**
     * 处理请求体参数校验异常
     *
     * @param e 参数校验异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e 参数绑定异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理上传大小超限异常
     *
     * @param e 上传大小超限异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("上传文件超过大小限制: {}", e.getMessage());
        return Result.error(400, "上传文件超过大小限制");
    }

    /**
     * 处理多部分上传异常
     *
     * @param e 多部分上传异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MultipartException.class)
    public Result<String> handleMultipartException(MultipartException e) {
        log.error("文件上传请求异常: {}", e.getMessage());
        return Result.error(400, "上传请求格式错误或文件超过限制");
    }

    /**
     * 兜底处理未捕获异常
     *
     * @param e 系统异常
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常，请联系管理员");
    }
}
