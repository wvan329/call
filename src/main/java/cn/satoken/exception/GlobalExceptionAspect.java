package cn.satoken.exception;

import cn.satoken.util.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GlobalExceptionAspect {
    @Around("@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public Object protectExceptionHandlers(ProceedingJoinPoint joinPoint) {
        try {
            // 正常执行 @ExceptionHandler 方法
            return joinPoint.proceed();
        } catch (Throwable ex) {
            // 返回统一错误响应（根据你的返回类型定制）
            return Result.error("服务器异常");
        }
    }
}
