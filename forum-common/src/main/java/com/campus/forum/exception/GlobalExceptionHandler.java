package com.campus.forum.exception;

import com.campus.forum.entity.Result;
import com.campus.forum.constant.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理各类异常，返回标准化的响应结果
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e       业务异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid）
     *
     * @param e       参数校验异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = extractValidationMessage(e.getBindingResult().getFieldErrors());
        log.warn("参数校验异常: {} - {}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e       参数绑定异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = extractValidationMessage(e.getBindingResult().getFieldErrors());
        log.warn("参数绑定异常: {} - {}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_BIND_ERROR.getCode(), message);
    }

    /**
     * 处理约束违反异常
     *
     * @param e       约束违反异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束违反异常: {} - {}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e       缺少请求参数异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = "缺少必要的请求参数: " + e.getParameterName();
        log.warn("缺少请求参数异常: {} - {}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_MISSING_ERROR.getCode(), message);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e       参数类型不匹配异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = "参数类型不匹配: " + e.getName();
        log.warn("参数类型不匹配异常: {} - {}", request.getRequestURI(), message);
        return Result.fail(ResultCode.PARAM_TYPE_ERROR.getCode(), message);
    }

    /**
     * 处理请求体不可读异常
     *
     * @param e       请求体不可读异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体不可读异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.REQUEST_BODY_ERROR.getCode(), "请求体格式错误");
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e       请求方法不支持异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.METHOD_NOT_ALLOWED.getCode(), "不支持的请求方法: " + e.getMethod());
    }

    /**
     * 处理404异常
     *
     * @param e       404异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("404异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.NOT_FOUND.getCode(), "请求的资源不存在");
    }

    /**
     * 处理文件上传大小超限异常
     *
     * @param e       文件上传大小超限异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传大小超限异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.FILE_SIZE_EXCEEDED.getCode(), "上传文件大小超过限制");
    }

    /**
     * 处理运行时异常
     *
     * @param e       运行时异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.fail(ResultCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后重试");
    }

    /**
     * 处理所有未捕获的异常
     *
     * @param e       异常
     * @param request 请求对象
     * @return 响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.fail(ResultCode.SYSTEM_ERROR.getCode(), "系统异常，请联系管理员");
    }

    /**
     * 提取校验错误信息
     *
     * @param fieldErrors 字段错误列表
     * @return 错误信息
     */
    private String extractValidationMessage(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }
}
