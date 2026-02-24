package org.example.shopdemo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.shopdemo.annotation.RequireAdmin;
import org.example.shopdemo.enums.UserRole;
import org.example.shopdemo.exception.PermissionDeniedException;
import org.example.shopdemo.utils.jwtutil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 管理员权限切面
 * 拦截带有@RequireAdmin注解的方法，检查用户是否有管理员权限
 */
@Aspect
@Component
public class AdminPermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminPermissionAspect.class);

    @Around("@annotation(requireAdmin)")
    public Object checkAdminPermission(ProceedingJoinPoint joinPoint, RequireAdmin requireAdmin) throws Throwable {
        // 获取当前HTTP请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new PermissionDeniedException("未登录，请先登录");
        }
        
        String actualToken = token.replace("Bearer ", "");
        Integer role = jwtutil.getRoleFromToken(actualToken);
        
        if (role == null) {
            log.error("无法从token中获取用户角色信息");
            throw new PermissionDeniedException("无法获取用户信息，请重新登录");
        }
        
        UserRole userRole = UserRole.fromCode(role);
        if (!userRole.isAdmin()) {
            log.warn("用户尝试访问管理员接口，角色: {}", userRole.getDescription());
            throw new PermissionDeniedException(requireAdmin.message());
        }
        //joinPoint.proceed() 表示继续执行被拦截的目标方法。
        return joinPoint.proceed();
    }
}
