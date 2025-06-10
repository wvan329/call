package cn.satoken.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.NotSafeException;
import cn.satoken.util.MyException;
import cn.satoken.util.Result;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.StringJoiner;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 请求方法不支持（如用 POST 调了 GET 接口）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error("请使用" + Arrays.toString(e.getSupportedMethods()) + "请求方式");
    }

    /**
     * 请求体解析失败：比如 LocalDateTime 类型不匹配
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        // Jackson 类型格式错误：如日期格式错误.
        // 某某字段的格式错误,比如数字给了abc,日期格式不符合要求(日期解析标准在配置类中配置了objectmapper)
        if (cause instanceof InvalidFormatException ife) {
            Object value = ife.getValue();
            StringJoiner fieldName = new StringJoiner("下的"); // 获取字段路径
            ife.getPath().get(0).getFieldName();
            for (JsonMappingException.Reference reference : ife.getPath()) {
                fieldName.add(reference.getFieldName());
            }
            return Result.error("[" + fieldName + "] 格式错误: " + value);
        }

        // Jackson 输入结构错误：如某个字段名输入错误,user写成usr
        if (cause instanceof MismatchedInputException mie) {
            StringJoiner fieldName = new StringJoiner("下的"); // 获取字段路径
            mie.getPath().get(0).getFieldName();
            for (JsonMappingException.Reference reference : mie.getPath()) {
                fieldName.add(reference.getFieldName());
            }
            return Result.error("字段名错误: [" + fieldName + "]");
        }

        // Jackson 语法错误：如 JSON字符串 不完整或非法字符
        if (cause instanceof JsonParseException jpe) {
            return Result.error("JSON格式错误");
        }
        throw e;
    }

    /**
     * 请求参数校验失败：比如 @Valid 注解校验失败.如:[username:不能为空] [password:不能为空]
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        StringBuilder msg = new StringBuilder("参数校验错误: ");
        e.getBindingResult().getFieldErrors().forEach(error ->
                msg.append("[").append(error.getField()).append(": ").append(error.getDefaultMessage()).append("] ")
        );
        return Result.error(msg.toString());
    }

    /**
     * @Validated 参数级校验失败（如 @RequestParam）,可以校验list集合.
     * 如: 参数校验错误:xxx
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolation(ConstraintViolationException e) {
        return Result.error("参数校验错误: " + e.getMessage());
    }

    /**
     * 未登录
     */
    @ExceptionHandler(NotLoginException.class)
    public Result handleNotLoginException(Exception e) {
        return Result.error("未登录");
    }

    /**
     * 无权限
     */
    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public Result handleNotPermissionException(Exception e) {
        return Result.error("无权限");
    }

    /**
     * 未二级校验
     */
    @ExceptionHandler(NotSafeException.class)
    public Result handleNotSafeException(Exception e) {
        return Result.error("未二级校验");
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(MyException.class)
    public Result handleMyExceptions(Exception e) {
        return Result.error(e.getMessage());
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result handleOtherExceptions(Exception e) {
        log.error(e.getMessage());
        return Result.error("服务器异常");
    }
}
