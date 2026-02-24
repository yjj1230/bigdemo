package org.example.shopdemo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 接口日志切面
 * 统一记录接口调用的日志
 */
@Aspect
@Component
public class ApiLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ApiLogAspect.class);

    /**
     * 定义切点：拦截所有Controller层的公共方法
     */
    @Pointcut("execution(public * org.example.shopdemo.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录接口调用的开始和结束
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        String uri = request != null ? request.getRequestURI() : "unknown";
        String method = request != null ? request.getMethod() : "unknown";
        String ip = request != null ? getClientIp(request) : "unknown";
        String username = request != null ? (String) request.getAttribute("username") : "anonymous";
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Object[] args = joinPoint.getArgs();
        String params = args.length > 0 ? args[0].toString() : "no params";
        
        logger.info(">>> 接口调用开始 - URI: {}, Method: {}, IP: {}, User: {}, Class: {}, Method: {}, Params: {}", 
                    uri, method, ip, username, className, methodName, params);
        
        Object result = null;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;
            
            logger.info("<<< 接口调用结束 - URI: {}, Method: {}, 耗时: {}ms, 结果: {}", 
                        uri, method, executeTime, result != null ? "success" : "null");
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;
            
            logger.error("!!! 接口调用异常 - URI: {}, Method: {}, 耗时: {}ms, 异常: {}", 
                       uri, method, executeTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取客户端IP地址
     * @param request HTTP请求对象
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
